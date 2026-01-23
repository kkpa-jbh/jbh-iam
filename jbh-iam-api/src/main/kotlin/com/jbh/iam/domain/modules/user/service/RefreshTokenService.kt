package com.jbh.iam.domain.modules.user.service

import com.jbh.iam.domain.modules.user.model.RefreshTokenEntity
import com.jbh.iam.domain.modules.user.model.UserEntity
import com.jbh.iam.domain.modules.user.repository.RefreshTokenRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Value("\${app.refreshTokenExpirationInMs}")
    private val refreshTokenExpirationInMs: Long = 0

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(RefreshTokenService::class.java)
    }

    @Transactional
    fun createRefreshToken(user: UserEntity, request: HttpServletRequest?): String {
        // Generate random token
        val token = UUID.randomUUID().toString()

        // Hash token before storing (never store plain tokens)
        val tokenHash = hashToken(token)

        // Extract device info
        val deviceInfo = request?.getHeader("User-Agent")?.take(255)
        val ipAddress = request?.remoteAddr

        // Create entity
        val refreshToken = RefreshTokenEntity(
            user = user,
            tokenHash = tokenHash,
            expiresAt = Instant.now().plusMillis(refreshTokenExpirationInMs),
            deviceInfo = deviceInfo,
            ipAddress = ipAddress
        )

        refreshTokenRepository.save(refreshToken)
        log.info("Created refresh token for user ${user.id}")

        return token // Return plain token to send to client
    }

    @Transactional
    fun validateAndGetUserId(token: String): UUID? {
        val tokenHash = hashToken(token)
        val refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)

        return when {
            refreshToken == null -> {
                log.warn("Refresh token not found")
                null
            }
            refreshToken.revoked -> {
                log.warn("Refresh token has been revoked")
                null
            }
            refreshToken.isExpired() -> {
                log.warn("Refresh token has expired")
                null
            }
            else -> {
                log.info("Refresh token validated for user ${refreshToken.user.id}")
                refreshToken.user.id
            }
        }
    }

    @Transactional
    fun revokeToken(token: String): Boolean {
        val tokenHash = hashToken(token)
        val refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)

        return if (refreshToken != null && !refreshToken.revoked) {
            refreshToken.revoke()
            refreshTokenRepository.save(refreshToken)
            log.info("Revoked refresh token")
            true
        } else {
            false
        }
    }

    @Transactional
    fun revokeAllUserTokens(userId: UUID): Int {
        val count = refreshTokenRepository.revokeAllByUserId(userId)
        log.info("Revoked $count refresh tokens for user $userId")
        return count
    }

    @Transactional
    fun cleanupExpiredTokens(): Int {
        val count = refreshTokenRepository.deleteExpiredTokens()
        log.info("Deleted $count expired refresh tokens")
        return count
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
