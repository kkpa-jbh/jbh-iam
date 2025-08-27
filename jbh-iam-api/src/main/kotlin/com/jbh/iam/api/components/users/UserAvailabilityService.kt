package com.jbh.iam.api.components.users

import com.jbh.iam.api.services.users.UserService
import com.jbh.iam.common.logging.customLogger
import org.springframework.stereotype.Service

@Service
class UserAvailabilityService(private val userService: UserService) {

    private val log = customLogger()

    fun checkUsernameAvailability(nickName: String): Boolean {
        log.info("Checking username availability")
        return !userService.existsByNickName(nickName)
    }

    fun checkEmailAvailability(email: String): Boolean {
        log.info("Checking email availability")
        return !userService.existsByEmail(email)
    }
}