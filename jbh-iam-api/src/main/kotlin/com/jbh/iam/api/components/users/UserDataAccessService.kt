package com.jbh.iam.api.components.users

import com.jbh.iam.api.services.users.UserService
import com.jbh.iam.core.access.IUserDataAccessService
import com.jbh.iam.core.model.UserCore
import com.jbh.iam.domain.modules.user.model.toUserCore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserDataAccessService : IUserDataAccessService {

    @Autowired
    lateinit var userService: UserService

    companion object {
        private val log = LoggerFactory.getLogger(IUserDataAccessService::class.java)
    }

    override fun findByNickNameOrEmail(nickName: String, email: String): UserCore {
        val user = userService.findByNickNameOrEmail(nickName, email)
        log.debug("loading user by userName... User Found! ${user.nickName}")
        return user.toUserCore()
    }

    override fun findByUserId(id: UUID): UserCore {
        val user = userService.findById(id)
        return user.toUserCore()
    }

    override fun fetchByUserNameOrEmail(userNameOrEmail: String): UserCore {
        val user = userService.fetchByUserNameOrEmail(userNameOrEmail)
        log.debug("loading user by userName... User Found! ${user.nickName}")
        return user.toUserCore()
    }
}