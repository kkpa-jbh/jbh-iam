package com.jbh.iam.api.components

import com.jbh.iam.api.Routes
import com.jbh.iam.api.components.auth.CookieService
import com.jbh.iam.api.components.users.UserAvailabilityService
import com.jbh.iam.api.components.users.UserRegistrationService
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.common.payload.ApiResponse
import com.jbh.iam.core.access.AuthenticationOperationService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Routes.LOGOUT_PATH)
class LogOutResource(
    private val cookieService: CookieService,
    private val authenticationOperationService: AuthenticationOperationService
) {
    private val log = customLogger()

    @PostMapping("")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<ApiResponse> {
        log.info("Logging out user ")


        // Invalidate session
        val session = request.getSession(false)
        with(session) {
            try {
                invalidate()
            }catch (e: Exception) {
                log.error("Error while invalidating session", e)
            }

            authenticationOperationService.logout()

            // Clear the authentication cookie
            cookieService.clearAuthCookie(response)

            return ResponseEntity.ok(ApiResponse(true, "Logged out successfully"))
        }

        return ResponseEntity.badRequest().body(ApiResponse(false, "Why?? :("))
    }
}