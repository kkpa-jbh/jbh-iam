package com.jbh.iam.api.components

import com.jbh.iam.api.Routes
import com.jbh.iam.api.components.auth.CookieService
import com.jbh.iam.cache.api.UserCacheOperations
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.common.payload.ApiResponse
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.domain.modules.user.service.RefreshTokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Routes.AUTH_PATH)
class LogOutResource(
    private val cookieService: CookieService,
    private val authenticationOperationService: AuthenticationOperationService,
    private val refreshTokenService: RefreshTokenService,
    private val userCache: UserCacheOperations
) {
    private val log = customLogger()

    @PostMapping(Routes.LOGOUT_PATH)
    fun logout(request: HttpServletRequest, response: HttpServletResponse,
               @RequestHeader("Authorization") authHeader: String): ResponseEntity<ApiResponse> {
        log.info("Logging out user ${request.userPrincipal}")

        try {
            val userId = authenticationOperationService.getUserIdFromJWT(authHeader)
            // Get and revoke refresh token
            val refreshToken = cookieService.getRefreshTokenFromCookie(request)
            if (refreshToken != null) {
                refreshTokenService.revokeToken(refreshToken)
                log.info("Refresh token revoked")
            }

            // Invalidate session
            request.getSession(false)?.invalidate()

            // Clear security context
            authenticationOperationService.logout()

            // Clear both cookies
            cookieService.clearAuthCookie(response)
            cookieService.clearRefreshTokenCookie(response)

            // Clear user cache
            userId?.let {
                userCache.evictUserId(it)
            }

            return ResponseEntity.ok(ApiResponse(true, "Logged out successfully"))
        } catch (e: Exception) {
            log.error("Error during logout", e)
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .body(ApiResponse(false, "Logout failed"))
        }
    }
}