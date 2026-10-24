package com.jbh.iam.domain.modules.user.repository

import com.jbh.iam.domain.modules.user.model.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {

    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?

    fun findAllByUserIdAndRevokedFalse(userId: UUID): List<RefreshTokenEntity>

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.user.id = :userId AND rt.revoked = false")
    fun revokeAllByUserId(userId: UUID, revokedAt: Instant = Instant.now()): Int

    /**
     * Revokes the token only if it is still active. Returns the number of revoked rows (0 or 1).
     * The database row lock makes it safe across many instances: only one caller gets 1.
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.tokenHash = :tokenHash AND rt.revoked = false")
    fun revokeIfActive(tokenHash: String, revokedAt: Instant): Int

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiresAt < :now")
    fun deleteExpiredTokens(now: Instant = Instant.now()): Int
}
