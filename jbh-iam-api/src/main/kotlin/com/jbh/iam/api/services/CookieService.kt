package com.jbh.iam.api.services

import com.jbh.iam.common.config.JBHConstants.JBH_TOKEN_COOKIE_NAME
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.core.environment.EnvironmentService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service

@Service
class CookieService(private val environment: EnvironmentService) {

    private val log = customLogger()

    fun clearAuthCookie(response: HttpServletResponse) {
        val cookie = Cookie(JBH_TOKEN_COOKIE_NAME, null)
        cookie.maxAge = 0
        cookie.path = "/"
        response.addCookie(cookie)
        log.info("Cookie has been cleared from the response")
    }

    fun setAuthCookie(response: HttpServletResponse, jwt: String) {
        val cookie = Cookie("__Secure-$JBH_TOKEN_COOKIE_NAME", jwt).apply {
            isHttpOnly = true
            maxAge = 3600
            path = "/"
            // SameSite is set to Strict to prevent CSRF attacks
            setAttribute("SameSite", "Strict")
            // In production, we should use a secure cookie
            secure = environment.isProd()
        }
        response.addCookie(cookie)
        log.info("Cookie has been added to the response")
    }
}