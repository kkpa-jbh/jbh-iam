package com.jbh.iam.security.authentication

import com.jbh.iam.common.config.JBHConstants
import com.jbh.iam.core.security.UserPrincipal
import io.jsonwebtoken.*
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

/**
 * The following utility class will be used for generating a JWT after a user logs in successfully,
 * and validating the JWT sent in the Authorization header of the requests -
 */
@Component
class JwtTokenProvider {

    @Value("\${app.jwtSecret}")
    private val jwtSecret: String? = null

    @Value("\${app.jwtExpirationInMs}")
    private val jwtExpirationInMs: Long = 0

    var log = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    fun generateToken(authentication: Authentication): String {
        log.info("Generating JWT Token... Expiration in ${jwtExpirationInMs} ms Secret is ${jwtSecret}")
        val userPrincipal: UserPrincipal = authentication.getPrincipal() as UserPrincipal

        log.info("User Principal is ${userPrincipal}")
        val now = Instant.now()
        val expiryDate = now.plusMillis(jwtExpirationInMs)

        return Jwts.builder()
            .setSubject(userPrincipal.id.toString())
            .setIssuedAt(Date.from(expiryDate))
            .setExpiration(Date.from(expiryDate))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact()
    }

    fun getUserIdFromJWT(token: String): UUID? {
        log.info("Getting User Id from JWT...")
        val claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .body

        return UUID.fromString(claims.subject)
    }

    fun validateToken(authToken: String): Boolean {
        log.info("Validating JWT Token...")
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken)
            return true
        } catch (ex: SignatureException) {
            log.error("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            log.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            log.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            log.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            log.error("JWT claims string is empty.")
        }

        return false
    }

    fun findJWTFromRequest(request: HttpServletRequest): String? {
        log.info("Getting JWT from request")

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
        log.info("Getting JWT from cookie")
        val cookies = request.cookies
        return cookies?.find { it.name == JBHConstants.JBH_TOKEN_COOKIE_NAME }?.value
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String {
        log.info("Getting JWT from request")
        val bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (bearerToken == null) {
            log.error("Authorization header is missing in the request for URI: ${request.requestURI}")
        }
        bearerToken.startsWith("Bearer ").let {
            return bearerToken.substring(7, bearerToken.length)
        }
    }
}