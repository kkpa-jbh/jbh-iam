package com.jbh.iam.api.components.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.jbh.iam.api.exceptions.web.AppException
import com.jbh.iam.api.util.DefaultValues
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.core.model.RoleName
import com.jbh.iam.core.security.IPasswordEncoder
import com.jbh.iam.domain.modules.user.model.UserEntity
import com.jbh.iam.domain.modules.user.repository.RoleRepository
import com.jbh.iam.domain.modules.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

class GoogleAuthException(message: String) : RuntimeException(message)

/**
 * Turns a Google ID token into a JBH user: finds it by Google id, links it by verified email,
 * or creates it. Issuing the JWT stays in AuthController, same as the password login.
 */
@Service
class GoogleAuthService(
    @Value("\${app.googleClientIds:}") clientIds: String,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    @Qualifier("jbhPasswordEncoder") private val jbhPasswordEncoder: IPasswordEncoder
) {

    private val log = customLogger()

    private val audience: List<String> = clientIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    // The verifier caches Google's public keys, so one instance is shared by all requests.
    // It checks the signature, the issuer, the expiry, and that aud is one of our client IDs.
    private val verifier: GoogleIdTokenVerifier =
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(audience)
            .build()

    @Transactional
    fun signIn(idToken: String): UserEntity {
        val payload = verify(idToken)
        val googleSub = payload.subject
        val email = payload.email

        userRepository.findByGoogleSub(googleSub)?.let { return it }

        userRepository.findByEmail(email)?.let { existing ->
            if (existing.googleSub != null) {
                // Same email, but another Google account is already linked. Never re-link silently.
                throw GoogleAuthException("Email is linked to another Google account")
            }
            log.info("Linking Google account to existing user ${existing.id}")
            existing.googleSub = googleSub
            return userRepository.save(existing)
        }

        return createUser(googleSub, email, payload["name"] as String?)
    }

    private fun verify(idToken: String): GoogleIdToken.Payload {
        if (audience.isEmpty()) {
            throw GoogleAuthException("Google sign-in is not configured")
        }
        val token = try {
            verifier.verify(idToken)
        } catch (e: Exception) {
            // Malformed token or Google keys not reachable. Both mean we cannot trust it.
            log.info("Google ID token could not be verified: ${e.message}")
            null
        } ?: throw GoogleAuthException("Invalid Google token")

        val payload = token.payload
        // An unverified email could belong to someone else, so it must never create or link an account.
        if (payload.emailVerified != true || payload.email.isNullOrBlank()) {
            throw GoogleAuthException("Google email is not verified")
        }
        return payload
    }

    private fun createUser(googleSub: String, email: String, name: String?): UserEntity {
        val nickName = uniqueNickName(email)
        val userRole = roleRepository.findByName(RoleName.ROLE_USER)
            .orElseThrow { AppException("User Role not set.") }

        val user = UserEntity(
            userName = displayName(name, email),
            email = email,
            nickName = nickName,
            // Random secret nobody knows: the column needs a value, and password login must fail.
            password = jbhPasswordEncoder.encode(UUID.randomUUID().toString()),
            googleSub = googleSub
        )
        user.roles = Collections.singleton(userRole)

        log.info("Creating user from Google sign-in, nickName $nickName")
        return userRepository.save(user)
    }

    private fun displayName(name: String?, email: String): String {
        val candidate = name?.trim().takeUnless { it.isNullOrEmpty() } ?: email.substringBefore("@")
        val padded = if (candidate.length < DefaultValues.MIN_DESC_NAMES_LENGTH) email else candidate
        return padded.take(DefaultValues.MAX_DESC_NAMES_LENGTH)
    }

    private fun uniqueNickName(email: String): String {
        val base = email.substringBefore("@").lowercase()
            .replace(Regex("[^a-z0-9._-]"), "")
            .padEnd(MIN_NICK_NAME_LENGTH, '0')
            .take(DefaultValues.MAX_DESC_NAMES_LENGTH - SUFFIX_LENGTH - 1)

        if (!userRepository.existsByNickName(base)) return base

        repeat(MAX_NICK_NAME_ATTEMPTS) {
            val candidate = "$base-${(1000..9999).random()}"
            if (!userRepository.existsByNickName(candidate)) return candidate
        }
        throw AppException("Could not generate a free nickName for $email")
    }

    companion object {
        private const val MIN_NICK_NAME_LENGTH = 3
        private const val SUFFIX_LENGTH = 4
        private const val MAX_NICK_NAME_ATTEMPTS = 10
    }
}
