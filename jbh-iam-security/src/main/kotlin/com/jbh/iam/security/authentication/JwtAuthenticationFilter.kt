package com.jbh.iam.security.authentication

import com.jbh.iam.common.extensions.withMetadata
import com.jbh.iam.security.authorization.AuthorizationRules
import com.jbh.iam.security.service.CustomUserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
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
        val uri = request.requestURI

        // Exclude static resources and public endpoints
        val isPublicEndpoint = isPublicEndpoint(uri)
        val isStaticResource = isStaticResource(uri)
        val isPublicPage = isPublicPage(uri)
        logger.info("Analyzing URI ${uri} PublicAPI: $isPublicEndpoint - StaticResource: $isStaticResource - PublicPage: $isPublicPage")
        if (!isPublicEndpoint && !isStaticResource && !isPublicPage) {
            try {
                tokenProvider.findJWTFromRequest(request)?.let {
                    logger.info("JWT from cookie: $it")
                    authenticateUserByJwt(request, it)
                }
            } catch (ex: Exception) {
                logger.error("Could not set user authentication in security context", ex)
                throw IllegalArgumentException("Request not valid")
            }
        }
        logger.info("Do Filter ${filterChain} - ${request}")
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
        val uriWithoutContextPath = uri.replaceFirst("/jbh", "");
        return AuthorizationRules.AUTH_API_WHITELIST.any { uriWithoutContextPath.startsWith(it.replace("/**", "")) }
    }

    private fun isPublicPage(uri: String): Boolean {
        return AuthorizationRules.AUTH_VIEW_WHITELIST.any { uri.startsWith(it) }
    }

    private fun isStaticResource(uri: String): Boolean {
        var result = withMetadata("url" to uri) {
            val staticPaths = listOf("/js/", "/css/", "/images/", "/webjars/")
            return@withMetadata staticPaths.any { uri.contains(it) }
        }
        return result
    }

}