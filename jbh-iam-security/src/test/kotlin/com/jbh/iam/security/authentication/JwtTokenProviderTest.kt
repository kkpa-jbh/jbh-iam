package com.jbh.iam.security.authentication

import com.jbh.iam.core.security.UserPrincipal
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.util.ReflectionTestUtils
import java.util.UUID
import jakarta.servlet.http.Cookie

/**
 * jbh-gateway checks the tokens made here (JwtValidator). Its test (TestTokens) builds tokens
 * the same way: Base64 secret, HS256, subject = user id, token_type = ACCESS. Keep both in step.
 */
class JwtTokenProviderTest {

    private val secret = "amJoLXRlc3Qtc2VjcmV0LW9ubHktZm9yLXVuaXQtdGVzdHMtMzIrYnl0ZXM=" // test value only
    private val userId = UUID.fromString("3f2b8c1e-4d5a-4b6c-9e7f-0a1b2c3d4e5f")

    private fun provider(expirationMs: Long = 60_000) = JwtTokenProvider().also {
        ReflectionTestUtils.setField(it, "jwtSecret", secret)
        ReflectionTestUtils.setField(it, "jwtExpirationInMs", expirationMs)
    }

    private fun authentication(): UsernamePasswordAuthenticationToken {
        val principal = UserPrincipal(userId, null, "Test User", "tester", "tester@example.com", "hash", emptyList())
        return UsernamePasswordAuthenticationToken(principal, null, emptyList())
    }

    @Test
    fun `token carries the user id, the ACCESS type and HS256`() {
        val token = provider().generateToken(authentication())

        val jws = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .build()
            .parseSignedClaims(token)

        assertThat(jws.header.algorithm).isEqualTo("HS256")
        assertThat(jws.payload.subject).isEqualTo(userId.toString())
        assertThat(jws.payload["token_type"]).isEqualTo("ACCESS")
    }

    @Test
    fun `round trip - validate and read the user id`() {
        val provider = provider()
        val token = provider.generateToken(authentication())

        assertThat(provider.validateToken(token)).isTrue()
        assertThat(provider.getUserIdFromJWT(token)).isEqualTo(userId)
    }

    @Test
    fun `expired, tampered and empty tokens are not valid`() {
        val provider = provider()
        val expired = provider(expirationMs = -60_000).generateToken(authentication())
        val token = provider.generateToken(authentication())
        val tampered = token.dropLast(2) + if (token.endsWith("AA")) "BB" else "AA"

        assertThat(provider.validateToken(expired)).isFalse()
        assertThat(provider.validateToken(tampered)).isFalse()
        assertThat(provider.validateToken("")).isFalse()
    }

    @Test
    fun `reads the token from the header first, then from the cookie`() {
        val provider = provider()

        val withHeader = MockHttpServletRequest().apply { addHeader("Authorization", "Bearer abc") }
        val withCookie = MockHttpServletRequest().apply { setCookies(Cookie("__Secure-JBH_TOKEN", "xyz")) }
        val withNothing = MockHttpServletRequest()

        assertThat(provider.findJWTFromRequest(withHeader)).isEqualTo("abc")
        assertThat(provider.findJWTFromRequest(withCookie)).isEqualTo("xyz")
        assertThat(provider.findJWTFromRequest(withNothing)).isNull()
    }
}
