package com.jbh.iam.security.authentication.service

import com.jbh.iam.common.exceptions.ForbiddenException
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.security.authentication.JwtTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

@Configuration
class AuthenticationServiceImpl : AuthenticationOperationService {

    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Autowired
    lateinit var tokenProvider: JwtTokenProvider

    @Autowired
    lateinit var customUserDetailsService: com.jbh.iam.security.service.CustomUserDetailsService

    companion object {
        val log = org.slf4j.LoggerFactory.getLogger(AuthenticationServiceImpl::class.java)
    }

    override fun authenticate(usernameOrEmail: String, password: String): String {
        log.info("Authenticating user $usernameOrEmail")
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                usernameOrEmail,
                password
            )
        )
        SecurityContextHolder.getContext().authentication = authentication

        val jwt = tokenProvider.generateToken(authentication)
        log.info("Authentication successful. Returning JWT token.")

        return jwt // Change to an object with the result
    }

    override fun logout() {
        log.debug("Clering the security context")
        // Only affects the current request.
        SecurityContextHolder.clearContext()
    }

    override fun getUserIdFromJWT(token: String): UUID? {
        return tokenProvider.getUserIdFromJWT(token)
    }

    override fun generateAccessTokenForUser(userId: UUID): String {
        log.info("Generating access token for user $userId")

        // Load user details
        val userDetails = customUserDetailsService.loadUserById(userId)

        // Create authentication object
        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )

        // Generate and return JWT token
        return tokenProvider.generateToken(authentication)
    }

    override fun requireAdminRole() {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw ForbiddenException("User is not authenticated")

        val hasAdminRole = authentication.authorities?.any {
            it.authority == "ROLE_ADMIN"
        } ?: false

        if (!hasAdminRole) {
            log.warn("Access denied: User does not have ADMIN role. Authorities: ${authentication.authorities}")
            throw ForbiddenException("User does not have ADMIN role")
        }

        log.info("Admin role verified for user: ${authentication.name}")
    }

}