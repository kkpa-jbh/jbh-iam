package com.jbh.iam.api.components.auth

import com.jbh.iam.core.environment.EnvironmentService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.env.MockEnvironment
import org.springframework.mock.web.MockHttpServletResponse

class CookieServiceTest {

    private fun cookieService(profile: String) =
        CookieService(EnvironmentService(MockEnvironment().apply { setActiveProfiles(profile) }))

    @Test
    fun `production cookie is secure, HttpOnly, SameSite=Lax, with the __Secure- prefix`() {
        val response = MockHttpServletResponse()

        cookieService("prod").setAuthCookie(response, "jwt")

        val cookie = response.getCookie("__Secure-JBH_TOKEN")!!
        assertThat(cookie.value).isEqualTo("jwt")
        assertThat(cookie.secure).isTrue()
        assertThat(cookie.isHttpOnly).isTrue()
        assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax")
    }

    @Test
    fun `local cookie has no prefix and works over http`() {
        val response = MockHttpServletResponse()

        cookieService("local").setRefreshTokenCookie(response, "refresh")

        val cookie = response.getCookie("JBH_REFRESH_TOKEN")!!
        assertThat(cookie.secure).isFalse()
        assertThat(cookie.isHttpOnly).isTrue()
        assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax")
    }
}
