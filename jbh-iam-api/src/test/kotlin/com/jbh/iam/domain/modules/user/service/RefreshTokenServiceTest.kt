package com.jbh.iam.domain.modules.user.service

import com.jbh.iam.domain.modules.user.model.RefreshTokenEntity
import com.jbh.iam.domain.modules.user.model.UserEntity
import com.jbh.iam.domain.modules.user.repository.RefreshTokenRepository
import com.jbh.iam.domain.modules.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.time.Instant
import java.util.UUID

/** Refresh token rotation: only the call that revokes the old token gets a new one. */
class RefreshTokenServiceTest {

    private val userId = UUID.randomUUID()
    private val refreshTokenRepository = mock(RefreshTokenRepository::class.java)
    private val service = RefreshTokenService(refreshTokenRepository, mock(UserRepository::class.java))

    private fun storedToken(expiresAt: Instant = Instant.now().plusSeconds(60)) =
        RefreshTokenEntity(user = UserEntity(id = userId), tokenHash = "hash", expiresAt = expiresAt)

    // any() returns null; the elvis gives Kotlin a non-null value while Mockito keeps the matcher.
    private fun revokeIfActiveReturns(rows: Int) {
        `when`(refreshTokenRepository.revokeIfActive(anyString(), any(Instant::class.java) ?: Instant.now()))
            .thenReturn(rows)
    }

    @Test
    fun `rotate gives nothing when the token was already revoked by a parallel call`() {
        `when`(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(storedToken())
        revokeIfActiveReturns(0)

        assertThat(service.rotate("old", null)).isNull()
        verify(refreshTokenRepository, never()).save(any(RefreshTokenEntity::class.java) ?: RefreshTokenEntity())
    }

    @Test
    fun `rotate gives a new token for the same user when the old one is revoked`() {
        `when`(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(storedToken())
        revokeIfActiveReturns(1)

        val rotated = service.rotate("old", null)

        assertThat(rotated).isNotNull
        assertThat(rotated!!.userId).isEqualTo(userId)
        assertThat(rotated.refreshToken).isNotBlank().isNotEqualTo("old")
        verify(refreshTokenRepository).save(any(RefreshTokenEntity::class.java) ?: RefreshTokenEntity())
    }

    @Test
    fun `rotate gives nothing for an unknown or expired token`() {
        `when`(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(null)
        assertThat(service.rotate("unknown", null)).isNull()

        `when`(refreshTokenRepository.findByTokenHash(anyString()))
            .thenReturn(storedToken(expiresAt = Instant.now().minusSeconds(1)))
        assertThat(service.rotate("expired", null)).isNull()
    }
}
