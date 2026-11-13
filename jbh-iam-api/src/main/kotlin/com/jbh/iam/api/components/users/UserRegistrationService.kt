package com.jbh.iam.api.components.users

import com.jbh.iam.api.services.users.UserService
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.common.payload.SignUpRequest
import com.jbh.iam.core.security.IPasswordEncoder
import com.jbh.iam.domain.modules.user.model.UserEntity
import com.jbh.iam.domain.modules.user.service.RoleService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserRegistrationService(
    private val userService: UserService,
    private val roleService: RoleService,
    @Qualifier("jbhPasswordEncoder") private val jbhPasswordEncoder: IPasswordEncoder
) {

    private val log = customLogger()

    companion object {
        // Keep in sync with the signup form in jbh-app (signup.page.ts).
        const val MIN_PASSWORD_LENGTH = 8
    }

    fun registerUser(signUpRequest: SignUpRequest): String {
        log.info("Registering user")

        if (signUpRequest.username.isBlank() || signUpRequest.email.isBlank()) {
            throw IllegalArgumentException("Username and email are required")
        }

        if (signUpRequest.password.length < MIN_PASSWORD_LENGTH) {
            throw IllegalArgumentException("Password must have at least $MIN_PASSWORD_LENGTH characters")
        }

        if (userService.existsByNickName(signUpRequest.username)) {
            throw IllegalArgumentException("NickName is already taken!")

        }

        if (userService.existsByEmail(signUpRequest.email)) {
            throw IllegalArgumentException("Email Address already in use!")
        }

        // Creating user's accountId
        val user = UserEntity(
            userName = signUpRequest.name, nickName = signUpRequest.username,
            email = signUpRequest.email, password = signUpRequest.password
        )

        user.password = jbhPasswordEncoder.encode(user.password)

        user.roles = Collections.singleton(roleService.defaultUserRole())

        userService.save(user)
        return "true"
    }

}