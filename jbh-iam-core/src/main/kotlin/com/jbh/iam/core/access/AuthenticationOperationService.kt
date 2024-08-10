package com.jbh.iam.core.access

import org.springframework.stereotype.Component
import java.util.*

@Component
interface AuthenticationOperationService {
    fun authenticate(usernameOrEmail: String, password: String): String

    fun logout()

    fun getUserIdFromJWT(token: String): UUID?
}