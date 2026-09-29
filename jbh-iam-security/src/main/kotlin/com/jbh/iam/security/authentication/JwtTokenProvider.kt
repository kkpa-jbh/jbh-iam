package com.jbh.iam.security.authentication

import com.jbh.iam.common.config.JBHConstants
import com.jbh.iam.core.security.UserPrincipal
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

private const val ACCESS_TOKEN_TYPE = "ACCESS"
private const val TOKEN_TYPE_CLAIM = "token_type"

/**
 * Creates the JWT after a successful login and validates the JWT sent in the requests.
 *
 * The secret (env `JWT_SECRET`) is Base64. `jbh-gateway` decodes it the same way to check the signature,
 * so both services must keep HS256 and the same decoding.
 */
@Component
class JwtTokenProvider {

    @Value("\${app.jwtSecret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwtExpirationInMs}")
    private val jwtExpirationInMs: Long = 0

    private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
    }

    fun generateToken(authentication: Authentication): String {
        log.debug("Generating JWT token. Expiration in {} ms", jwtExpirationInMs)
        val userPrincipal: UserPrincipal = authentication.principal as UserPrincipal
        log.debug("Generating JWT token for user {}", userPrincipal.id)

        val now = Instant.now()
        val expiryDate = now.plusMillis(jwtExpirationInMs)

        return Jwts.builder()
            .subject(userPrincipal.id.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiryDate))
            .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact()
    }

    fun getUserIdFromJWT(token: String): UUID? {
        log.debug("Getting User Id from JWT...")
        return UUID.fromString(parseClaims(token).subject)
    }

    fun validateToken(authToken: String): Boolean {
        log.debug("Validating JWT Token...")
        try {
            parseClaims(authToken)
            return true
        } catch (ex: ExpiredJwtException) {
            log.error("Expired JWT token")
        } catch (ex: JwtException) {
            log.error("Invalid JWT token: {}", ex.javaClass.simpleName)
        } catch (ex: IllegalArgumentException) {
            log.error("JWT claims string is empty.")
        }

        return false
    }

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload

    fun findJWTFromRequest(request: HttpServletRequest): String? {
        log.debug("Getting JWT from request")

        var jwt = getJwtFromRequest(request)
        if (jwt.isBlank()) {
            jwt = getJwtFromCookie(request) ?: return null
        }
        if (jwt.isBlank()) {
            throw IllegalArgumentException("JWT not found")
        }
        return jwt
    }

    private fun getJwtFromCookie(request: HttpServletRequest): String? {
        log.debug("Getting JWT from cookie")
        val cookies = request.cookies
        // Look for both the secure and non-secure cookie names
        return cookies?.find {
            it.name == "__Secure-${JBHConstants.JBH_TOKEN_COOKIE_NAME}" ||
            it.name == JBHConstants.JBH_TOKEN_COOKIE_NAME
        }?.value
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String {
        log.debug("Getting JWT from request")
        val bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (bearerToken == null) {
            log.debug("Authorization header is missing in the request for URI: ${request.requestURI}")
            return ""
        }
        return if (bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            log.debug("Authorization header does not start with Bearer for URI: ${request.requestURI}")
            ""
        }
    }
}
