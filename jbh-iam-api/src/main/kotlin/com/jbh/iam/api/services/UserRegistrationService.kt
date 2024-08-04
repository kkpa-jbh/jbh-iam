package com.jbh.iam.api.services

import com.jbh.iam.api.domain.Users.User
import com.jbh.iam.api.dto.UserGroupDTO
import com.jbh.iam.api.exceptions.web.AppException
import com.jbh.iam.api.repositories.users.RoleRepository
import com.jbh.iam.api.services.users.UserGroupServiceImpl
import com.jbh.iam.api.services.users.UserService
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.common.payload.SignUpRequest
import com.jbh.iam.core.model.RoleName
import com.jbh.iam.core.security.IPasswordEncoder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserRegistrationService(
    private val userService: UserService,
    private val roleRepository: RoleRepository,
    @Qualifier("jbhPasswordEncoder") private val jbhPasswordEncoder: IPasswordEncoder,
    private val userGroupServiceImpl: UserGroupServiceImpl,
) {

    private val log = customLogger()

    fun registerUser(signUpRequest: SignUpRequest): String {
        log.info("Registering user")

        if (userService.existsByNickName(signUpRequest.username)) {
            throw IllegalArgumentException("NickName is already taken!")

        }

        if (userService.existsByEmail(signUpRequest.email)) {
            throw IllegalArgumentException("Email Address already in use!")
        }

        // Creating user's accountId
        val user = User(
            userName = signUpRequest.name, nickName = signUpRequest.username,
            email = signUpRequest.email, password = signUpRequest.password
        )

        user.password = jbhPasswordEncoder.encode(user.password)

        val userRole = roleRepository.findByName(RoleName.ROLE_USER)
            .orElseThrow { AppException("User Role not set.") }

        user.roles = (Collections.singleton(userRole))

        userGroupServiceImpl.save(UserGroupDTO(name = user.nickName, userOwner = user.toDTO()))

        // Saving default account
        //TODO save account
        return "true"
    }

}