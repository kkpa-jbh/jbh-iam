package com.jbh.iam.api.controllers.authentication


import com.jbh.iam.api.Routes
import com.jbh.iam.api.domain.Users.User
import com.jbh.iam.api.dto.AccountsDTO
import com.jbh.iam.api.dto.UserGroupDTO
import com.jbh.iam.api.exceptions.web.AppException
import com.jbh.iam.api.repositories.users.RoleRepository
import com.jbh.iam.api.services.accounts.AccountServiceImpl
import com.jbh.iam.api.services.users.UserGroupServiceImpl
import com.jbh.iam.api.services.users.UserServiceImpl
import com.jbh.iam.common.config.JBHConstants.JBH_TOKEN_COOKIE_NAME
import com.jbh.iam.common.payload.*
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.core.model.RoleName
import com.jbh.iam.core.security.IPasswordEncoder
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*


@RestController
@RequestMapping(Routes.AUTH_PATH)
class AuthController {

    @Autowired
    lateinit var authenticationOperationService: AuthenticationOperationService

    @Autowired
    lateinit var userGroupServiceImpl: UserGroupServiceImpl

    @Autowired
    lateinit var userService: UserServiceImpl

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var passwordEncoder: IPasswordEncoder


    @Autowired
    lateinit var accountServiceImpl: AccountServiceImpl


    companion object {
        private val log = LoggerFactory.getLogger(AuthController::class.java)
    }

    @PostMapping("/signin")
    fun authenticateUser(
        @RequestBody loginRequest: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<JwtAuthenticationResponse> {

        log.info("Authenticating user")
        val jwt = authenticationOperationService.authenticate(
            loginRequest.usernameOrEmail!!,
            loginRequest.password!!
        )


        val cookie = Cookie(JBH_TOKEN_COOKIE_NAME, jwt)
        cookie.isHttpOnly = true
        cookie.maxAge = 3600
        cookie.path = "/"
        // SameSite is set to Strict to prevent CSRF attacks
        cookie.setAttribute("SameSite", "Strict")

        // For CSRF protection, Implement Spring Security CSRF Token

        // In production, we should use a secure cookie
        // cookie.secure = true

        response.addCookie(cookie)

        log.info("Cookie has been added to the response")


        return ResponseEntity.ok<JwtAuthenticationResponse>(JwtAuthenticationResponse(jwt))
    }

    @PostMapping("/signup")
    fun registerUser(@RequestBody signUpRequest: SignUpRequest): ResponseEntity<*> {
        log.info("Registering user")
        if (userService.existsByNickName(signUpRequest.username)) {
            return ResponseEntity(
                ApiResponse(false, "NickName is already taken!"),
                HttpStatus.BAD_REQUEST
            )
        }

        if (userService.existsByEmail(signUpRequest.email)) {
            return ResponseEntity(
                ApiResponse(false, "Email Address already in use!"),
                HttpStatus.BAD_REQUEST
            )
        }

        // Creating user's accountId
        val user = User(
            userName = signUpRequest.name, nickName = signUpRequest.username,
            email = signUpRequest.email, password = signUpRequest.password
        )

        user.password = passwordEncoder.encode(user.password)

        val userRole = roleRepository.findByName(RoleName.ROLE_USER)
            .orElseThrow { AppException("User Role not set.") }

        user.roles = (Collections.singleton(userRole))

        val userGroupCreated = userGroupServiceImpl.save(UserGroupDTO(name = user.nickName, userOwner = user.toDTO()))

        // Saving default account
        val defaultAccountDTO = AccountsDTO(
            description = "Default Account for ${userGroupCreated.userOwner?.nickName}",
            userGroup = userGroupCreated
        )

        try {
            accountServiceImpl.save(defaultAccountDTO)
        } catch (e: Exception) {
            log.error("Error creating the account", e)
            return ResponseEntity.badRequest().body<Any>(ApiResponse(false, "Error creating the account"))
        }


        val location = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/api/users/{username}")
            .buildAndExpand(userGroupCreated.userOwner?.nickName).toUri()

        return ResponseEntity.created(location).body<Any>(ApiResponse(true, "User registered successfully"))
    }


    @GetMapping("/checkUsernameAvailability")
    fun checkUsernameAvailability(@RequestParam(value = "nickName") nickName: String): ResponseEntity<UserIdentityAvailabilityResponse> {
        val isAvailable = !userService.existsByNickName(nickName)
        return ResponseEntity.ok(UserIdentityAvailabilityResponse(isAvailable))
    }

    @GetMapping("/checkEmailAvailability")
    fun checkEmailAvailability(@RequestParam(value = "email") email: String): ResponseEntity<UserIdentityAvailabilityResponse> {
        val isAvailable = !userService.existsByEmail(email)
        return ResponseEntity.ok(UserIdentityAvailabilityResponse(isAvailable))
    }
}