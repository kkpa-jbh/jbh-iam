package com.jbh.iam.core.access

import org.springframework.stereotype.Component
import java.util.*

@Component
interface AuthenticationOperationService {
    fun authenticate(usernameOrEmail: String, password: String): String

    fun logout()

    fun getUserIdFromJWT(token: String): UUID?

    /**
     * Generates a new access token for the given user ID.
     * Used by the refresh token flow.
     */
    fun generateAccessTokenForUser(userId: UUID): String

    /**
     * Checks if the current authenticated user has ADMIN role.
     * Throws AccessDeniedException if user is not an admin or not authenticated.
     */
    fun requireAdminRole()
}