package com.jbh.iam.domain.modules.user.service

import com.jbh.iam.domain.modules.user.model.RefreshTokenEntity
import com.jbh.iam.domain.modules.user.model.UserEntity
import com.jbh.iam.domain.modules.user.repository.RefreshTokenRepository
import com.jbh.iam.domain.modules.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.*

/** Result of a refresh token rotation: the owner and the new plain token. */
data class RotatedRefreshToken(val userId: UUID, val refreshToken: String)

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRepository: UserRepository
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
        log.info("Created refresh token for user {}", user.id)

        return token // Return plain token to send to client
    }

    /** For a user who just signed in. A reference is enough: the user exists, so no extra select. */
    @Transactional
    fun createRefreshToken(userId: UUID, request: HttpServletRequest?): String =
        createRefreshToken(userRepository.getReferenceById(userId), request)

    /**
     * Revokes the old token and creates a new one for the same user, in one transaction.
     * Returns null when the old token is unknown, expired, or already used.
     * Two parallel calls with the same token: the database lets only one of them win.
     */
    @Transactional
    fun rotate(oldToken: String, request: HttpServletRequest?): RotatedRefreshToken? {
        val tokenHash = hashToken(oldToken)
        val stored = refreshTokenRepository.findByTokenHash(tokenHash)
        if (stored == null || stored.isExpired()) {
            log.warn("Refresh token not found or expired")
            return null
        }

        if (refreshTokenRepository.revokeIfActive(tokenHash, Instant.now()) != 1) {
            log.warn("Refresh token was already revoked or used")
            return null
        }

        val userId = stored.user.id ?: return null
        val newToken = createRefreshToken(stored.user, request)
        log.info("Refresh token rotated for user {}", userId)
        return RotatedRefreshToken(userId, newToken)
    }

    @Transactional
    fun revokeToken(token: String): Boolean {
        val revoked = refreshTokenRepository.revokeIfActive(hashToken(token), Instant.now()) == 1
        if (revoked) {
            log.info("Revoked refresh token")
        }
        return revoked
    }

    @Transactional
    fun revokeAllUserTokens(userId: UUID): Int {
        val count = refreshTokenRepository.revokeAllByUserId(userId)
        log.info("Revoked {} refresh tokens for user {}", count, userId)
        return count
    }

    @Transactional
    fun cleanupExpiredTokens(): Int {
        val count = refreshTokenRepository.deleteExpiredTokens()
        log.info("Deleted {} expired refresh tokens", count)
        return count
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(token.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
