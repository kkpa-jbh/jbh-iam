package com.jbh.iam.api.components.auth

import com.jbh.iam.api.components.users.UserAvailabilityService
import com.jbh.iam.api.components.users.UserRegistrationService
import com.jbh.iam.api.services.users.UserService
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.domain.modules.user.service.RefreshTokenService
import com.jbh.iam.domain.modules.user.service.RotatedRefreshToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.util.UUID

/** The refresh endpoint: a token that lost the rotation race must not get new tokens. */
class AuthControllerTest {

    private val userId = UUID.randomUUID()
    private val request = MockHttpServletRequest()
    private val response = MockHttpServletResponse()

    private val cookieService = mock(CookieService::class.java).also {
        `when`(it.getRefreshTokenFromCookie(request)).thenReturn("old")
    }
    private val refreshTokenService = mock(RefreshTokenService::class.java)
    private val authService = mock(AuthenticationOperationService::class.java)
    private val controller = AuthController(
        cookieService,
        mock(UserAvailabilityService::class.java),
        mock(UserRegistrationService::class.java),
        authService,
        mock(GoogleAuthService::class.java),
        refreshTokenService,
        mock(UserService::class.java)
    )

    @Test
    fun `refresh answers 401 when the old token could not be revoked`() {
        `when`(refreshTokenService.rotate("old", request)).thenReturn(null)

        val result = controller.refreshToken(request, response)

        assertThat(result.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
        verify(authService, never()).generateAccessTokenForUser(userId)
        verify(cookieService, never()).setAuthCookie(response, "access")
    }

    @Test
    fun `refresh issues new tokens when the old token was revoked`() {
        `when`(refreshTokenService.rotate("old", request)).thenReturn(RotatedRefreshToken(userId, "new-refresh"))
        `when`(authService.generateAccessTokenForUser(userId)).thenReturn("access")

        val result = controller.refreshToken(request, response)

        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body!!.accessToken).isEqualTo("access")
        assertThat(result.body!!.refreshToken).isEqualTo("new-refresh")
        verify(cookieService).setAuthCookie(response, "access")
        verify(cookieService).setRefreshTokenCookie(response, "new-refresh")
    }
}
