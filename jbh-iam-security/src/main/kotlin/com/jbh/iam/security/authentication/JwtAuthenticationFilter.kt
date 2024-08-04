package com.jbh.iam.security.authentication

import com.jbh.iam.common.config.JBHConstants.JBH_TOKEN_COOKIE_NAME
import com.jbh.iam.common.extensions.withMetadata
import com.jbh.iam.security.service.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * We’ll use JWTAuthenticationFilter to implement a filter that -

 *   reads JWT authentication token from the Authorization header of all the requests
 *   validates the token
 *   loads the user details associated with that token.
 *   Sets the user details in Spring Security’s SecurityContext.
 *  Spring Security uses the user details to perform authorization checks.
 *  We can also access the user details stored in the SecurityContext in our controllers to perform our business logic.
 */
class JwtAuthenticationFilter : OncePerRequestFilter() {

    @Autowired
    lateinit var tokenProvider: JwtTokenProvider

    @Autowired
    lateinit var customUserDetailsService: CustomUserDetailsService


    /**
     * We’re first parsing the JWT retrieved from the Authorization header of the request and obtaining the user’s Id. After that,
     * We’re loading the user’s details from the database and setting the authentication inside spring security’s context.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.info("Request URI: ${request.requestURI}")

        // Exclude static resources and public endpoints
        if (!isPublicEndpoint(request.requestURI) && !isStaticResource(request.requestURI)) {
            try {
                logger.info("Getting JWT from cookie")
                getJwtFromCookie(request)?.let {
                    logger.info("JWT from cookie: $it")
                    authenticateUserByJwt(request, it)
                }
            } catch (ex: Exception) {
                logger.error("Could not set user authentication in security context", ex)
                throw IllegalArgumentException("Request not valid")
            }
        }
        logger.info("Filter chain executed")
        filterChain.doFilter(request, response)
    }

    private fun authenticateUserByJwt(request: HttpServletRequest, jwt: String) {
        logger.info("Authenticating user by JWT")
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            val userId = tokenProvider.getUserIdFromJWT(jwt)
            val userDetails = customUserDetailsService.loadUserById(userId!!)
            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            logger.info("User Authenticated: ${authentication.name}")
            SecurityContextHolder.getContext().authentication = authentication
        }
    }

    private fun isPublicEndpoint(uri: String): Boolean {
        logger.info("Checking if $uri is public endpoint")
        return uri.startsWith("/jbh/auth/") || uri.startsWith("/jbh/view/auth")
    }

    private fun isStaticResource(uri: String): Boolean {
        withMetadata("url" to uri) {
            logger.info("Checking if $uri is static resource")
            val staticPaths = listOf("/js/", "/css/", "/images/", "/webjars/")
            return@withMetadata staticPaths.any { uri.contains(it) }
        }
        return false
    }

    @Deprecated("Use getJwtFromCookie instead")
    private fun getJwtFromRequest(request: HttpServletRequest): String {
        logger.info("Getting JWT from request")
        val bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (bearerToken == null) {
            logger.error("Authorization header is missing in the request for URI: ${request.requestURI}")
            throw IllegalArgumentException("Request not valid")
        }
        bearerToken.startsWith("Bearer ").let {
            return bearerToken.substring(7, bearerToken.length)
        }
    }

    private fun getJwtFromCookie(request: HttpServletRequest): String? {
        logger.info("Getting JWT from cookie")
        val cookies = request.cookies
        return cookies?.find { it.name == JBH_TOKEN_COOKIE_NAME }?.value
    }
}