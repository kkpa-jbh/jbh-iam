package com.jbh.iam.api.components.auth


import com.jbh.iam.api.Routes
import com.jbh.iam.api.components.users.UserAvailabilityService
import com.jbh.iam.api.components.users.UserRegistrationService
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.common.payload.*
import com.jbh.iam.core.access.AuthenticationOperationService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder


@RestController
@RequestMapping(Routes.AUTH_PATH)
class AuthController(
    private val cookieService: CookieService,
    private val userAvailabilitySrv: UserAvailabilityService,
    private val userRegistrationSrv: UserRegistrationService,
    private val authenticationOperationService: AuthenticationOperationService,
    private val googleAuthService: GoogleAuthService,
    private val refreshTokenService: com.jbh.iam.domain.modules.user.service.RefreshTokenService,
    private val userRepository: com.jbh.iam.domain.modules.user.repository.UserRepository
) {

    private val log = customLogger()

    @PostMapping("/signin")
    fun authenticateUser(
        @RequestBody loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<JwtAuthenticationResponse> {
        val usernameOrEmail = loginRequest.usernameOrEmail
        val password = loginRequest.password
        if (usernameOrEmail.isNullOrBlank() || password.isNullOrBlank()) {
            return ResponseEntity.badRequest()
                .body(JwtAuthenticationResponse(error = "usernameOrEmail and password are required"))
        }

        // Authenticate user and get access token
        val jwt = authenticationOperationService.authenticate(usernameOrEmail, password)

        // Get user entity to create refresh token
        val user = userRepository.findByNickNameOrEmail(usernameOrEmail, usernameOrEmail)
            ?: return ResponseEntity.status(401).body(JwtAuthenticationResponse(error = "User not found"))

        // Create refresh token
        val refreshToken = refreshTokenService.createRefreshToken(user, request)

        // Set both cookies
        cookieService.setAuthCookie(response, jwt)
        cookieService.setRefreshTokenCookie(response, refreshToken)

        return ResponseEntity.ok(JwtAuthenticationResponse(jwt, refreshToken))
    }

    @PostMapping("/google")
    fun authenticateWithGoogle(
        @RequestBody googleLoginRequest: GoogleLoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<JwtAuthenticationResponse> {
        val idToken = googleLoginRequest.idToken
        if (idToken.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(JwtAuthenticationResponse(error = "idToken is required"))
        }

        val user = try {
            googleAuthService.signIn(idToken)
        } catch (e: GoogleAuthException) {
            log.info("Google sign-in rejected: ${e.message}")
            return ResponseEntity.status(401).body(JwtAuthenticationResponse(error = e.message))
        }

        // From here on it is the same session as a password login.
        val jwt = authenticationOperationService.generateAccessTokenForUser(user.id!!)
        val refreshToken = refreshTokenService.createRefreshToken(user, request)

        cookieService.setAuthCookie(response, jwt)
        cookieService.setRefreshTokenCookie(response, refreshToken)

        return ResponseEntity.ok(JwtAuthenticationResponse(jwt, refreshToken))
    }

    @PostMapping(Routes.REFRESH_TOKEN_PATH)
    fun refreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<JwtAuthenticationResponse> {

        // Get refresh token from cookie
        val oldRefreshToken = cookieService.getRefreshTokenFromCookie(request)
            ?: return ResponseEntity.status(401)
                .body(JwtAuthenticationResponse(error = "Refresh token not found"))

        // Validate refresh token and get user ID
        val userId = refreshTokenService.validateAndGetUserId(oldRefreshToken)
            ?: return ResponseEntity.status(401)
                .body(JwtAuthenticationResponse(error = "Invalid or expired refresh token"))

        log.info("Refreshing Token  $userId " )
        // Revoke old refresh token (token rotation security practice)
        refreshTokenService.revokeToken(oldRefreshToken)

        // Get user entity to create new refresh token
        val user = userRepository.findById(userId).orElseThrow {
            IllegalStateException("User not found for ID: $userId")
        }

        // Create new refresh token (rotation)
        val newRefreshToken = refreshTokenService.createRefreshToken(user, request)

        // Generate new access token for the user
        val newAccessToken = authenticationOperationService.generateAccessTokenForUser(userId)

        // Set both new cookies
        cookieService.setAuthCookie(response, newAccessToken)
        cookieService.setRefreshTokenCookie(response, newRefreshToken)

        log.info("Token has been refreshed ")

        return ResponseEntity.ok(JwtAuthenticationResponse(newAccessToken, newRefreshToken))
    }

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: SignUpRequest): ResponseEntity<*> {
        log.info("Registering user ${signUpRequest.username}")

        try {
            userRegistrationSrv.registerUser(signUpRequest)
        } catch (illegalArgumentException: IllegalArgumentException) {
            return ResponseEntity.badRequest().body<Any>(ApiResponse(false, illegalArgumentException.message))
        }

        val location = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/api/users/{username}")
            .buildAndExpand(signUpRequest.username).toUri()

        return ResponseEntity.created(location).body<Any>(ApiResponse(true, "User registered successfully"))
    }




    @GetMapping("/checkUsernameAvailability")
    fun checkUsernameAvailability(@RequestParam(value = "nickName") nickName: String): ResponseEntity<UserIdentityAvailabilityResponse> {
        val isAvailable = userAvailabilitySrv.checkUsernameAvailability(nickName)
        return ResponseEntity.ok(UserIdentityAvailabilityResponse(isAvailable))
    }

    @GetMapping("/checkEmailAvailability")
    fun checkEmailAvailability(@RequestParam(value = "email") email: String): ResponseEntity<UserIdentityAvailabilityResponse> {
        val isAvailable = userAvailabilitySrv.checkEmailAvailability(email)
        return ResponseEntity.ok(UserIdentityAvailabilityResponse(isAvailable))
    }
}