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
        log.info("loading user by nickName or email... ")
        val user = userService.findByNickNameOrEmail(nickName, email)
        log.info("loading user by userName... User Found! ${user.nickName}")
        return user.toUserCore()
    }

    override fun findByUserId(id: UUID): UserCore {
        val user = userService.findById(id)

        log.info("loading user by ownerId... User Found! ${user.nickName}")
        return user.toUserCore()
    }
}