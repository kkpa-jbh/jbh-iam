package com.jbh.iam.api.components.auth

import com.jbh.iam.common.config.JBHConstants.JBH_REFRESH_TOKEN_COOKIE_NAME
import com.jbh.iam.common.config.JBHConstants.JBH_TOKEN_COOKIE_NAME
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.core.environment.EnvironmentService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service

@Service
class CookieService(private val environment: EnvironmentService) {

    private val log = customLogger()

    /**
     * Generates the appropriate cookie name based on the environment.
     * In production, adds the __Secure- prefix which requires HTTPS and secure=true.
     * In local/development, omits the prefix to allow HTTP cookies.
     */
    private fun getCookieName(baseName: String): String {
        return if (environment.isProd()) {
            "__Secure-$baseName"
        } else {
            baseName
        }
    }

    /**
     * Configures common security attributes for cookies based on the environment:
     * - SameSite: "Lax" for local (allows cross-origin on localhost), "None" for production (requires HTTPS)
     * - Secure: false for local (HTTP), true for production (HTTPS required)
     * - HttpOnly: always true (prevents JavaScript access)
     * - Path: always "/" (available across entire domain)
     */
    private fun Cookie.applySecuritySettings() {
        isHttpOnly = true
        path = "/"

        // SameSite configuration for cross-origin support:
        // - "Lax" for local/development: Allows cross-origin cookies on localhost
        //   while still providing CSRF protection for state-changing requests
        // - "None" for production: Required for cross-origin requests with HTTPS
        //   Note: SameSite=None REQUIRES secure=true (HTTPS)
        val sameSite = if (environment.isProd()) "None" else "Lax"
        setAttribute("SameSite", sameSite)

        // Secure flag: Required for SameSite=None, enables cookie encryption over HTTPS
        // In production with SameSite=None, this MUST be true
        secure = environment.isProd()
    }

    fun clearAuthCookie(response: HttpServletResponse) {
        // Clear both possible cookie names (with and without __Secure- prefix)
        // to ensure compatibility across environments
        val cookieNames = listOf(
            JBH_TOKEN_COOKIE_NAME,
            "__Secure-$JBH_TOKEN_COOKIE_NAME"
        )

        cookieNames.forEach { name ->
            val cookie = Cookie(name, null)
            cookie.maxAge = 0
            cookie.path = "/"
            response.addCookie(cookie)
        }
        log.info("Auth cookies have been cleared from the response")
    }

    fun setAuthCookie(response: HttpServletResponse, jwt: String) {
        val cookieName = getCookieName(JBH_TOKEN_COOKIE_NAME)
        val cookie = Cookie(cookieName, jwt).apply {
            maxAge = 900 // 15 minutes
            applySecuritySettings()
        }
        response.addCookie(cookie)
        log.info("Access token cookie '$cookieName' has been added to the response with SameSite=${cookie.getAttribute("SameSite")}, Secure=${cookie.secure}")
    }

    fun setRefreshTokenCookie(response: HttpServletResponse, refreshToken: String) {
        val cookieName = getCookieName(JBH_REFRESH_TOKEN_COOKIE_NAME)
        val cookie = Cookie(cookieName, refreshToken).apply {
            maxAge = 2592000 // 30 days in seconds
            applySecuritySettings()
        }
        response.addCookie(cookie)
        log.info("Refresh token cookie '$cookieName' has been added to the response with SameSite=${cookie.getAttribute("SameSite")}, Secure=${cookie.secure}")
    }

    fun clearRefreshTokenCookie(response: HttpServletResponse) {
        // Clear both possible cookie names (with and without __Secure- prefix)
        // to ensure compatibility across environments
        val cookieNames = listOf(
            JBH_REFRESH_TOKEN_COOKIE_NAME,
            "__Secure-$JBH_REFRESH_TOKEN_COOKIE_NAME"
        )

        cookieNames.forEach { name ->
            val cookie = Cookie(name, "")
            cookie.maxAge = 0
            cookie.path = "/"
            response.addCookie(cookie)
        }
        log.info("Refresh token cookies have been cleared from the response")
    }

    fun getRefreshTokenFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.find {
            it.name == "__Secure-$JBH_REFRESH_TOKEN_COOKIE_NAME" ||
            it.name == JBH_REFRESH_TOKEN_COOKIE_NAME
        }?.value
    }
}