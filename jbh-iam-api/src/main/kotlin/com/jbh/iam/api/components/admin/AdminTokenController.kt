package com.jbh.iam.api.components.admin

import com.jbh.iam.api.Routes
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.common.payload.ApiResponse
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.domain.modules.user.service.RefreshTokenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(Routes.ADMIN_USERS_PATH)
class AdminTokenController(
    private val refreshTokenService: RefreshTokenService,
    private val authenticationOperationService: AuthenticationOperationService
) {

    private val log = customLogger()

    @PostMapping("/{userId}/revoke-tokens")
    fun revokeAllUserTokens(@PathVariable userId: UUID): ResponseEntity<ApiResponse> {
        // Manual admin role check
        authenticationOperationService.requireAdminRole()

        log.info("Admin revoking all tokens for user $userId")

        val count = refreshTokenService.revokeAllUserTokens(userId)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Revoked $count refresh token(s) for user $userId"
            )
        )
    }
}
