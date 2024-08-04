package com.jbh.iam.core.access

import org.springframework.stereotype.Component

@Component
interface AuthenticationOperationService {
    fun authenticate(usernameOrEmail: String, password: String): String

    fun logout()
}