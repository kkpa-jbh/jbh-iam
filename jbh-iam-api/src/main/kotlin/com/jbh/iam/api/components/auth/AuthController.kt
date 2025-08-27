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
    private val authenticationOperationService: AuthenticationOperationService
) {

    private val log = customLogger()

    @PostMapping("/signin")
    fun authenticateUser(
        @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<JwtAuthenticationResponse> {
        val jwt = authenticationOperationService.authenticate(
            loginRequest.usernameOrEmail!!,
            loginRequest.password!!
        )
        cookieService.setAuthCookie(response, jwt)

        return ResponseEntity.ok<JwtAuthenticationResponse>(JwtAuthenticationResponse(jwt))
    }

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: SignUpRequest): ResponseEntity<*> {
        log.debug("Registering user")

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

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<ApiResponse> {
        log.info("Logging out user ")


        // Invalidate session
        val session = request.getSession(false)
        with(session) {
            invalidate()
            authenticationOperationService.logout()

            // Clear the authentication cookie
            cookieService.clearAuthCookie(response)

            return ResponseEntity.ok(ApiResponse(true, "Logged out successfully"))
        }

        return ResponseEntity.badRequest().body(ApiResponse(false, "Why?? :("))
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