package com.jbh.iam.api.components.users

import com.jbh.iam.api.Routes
import com.jbh.iam.api.services.users.UserService
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.domain.modules.user.model.PublicUserInfo
import com.jbh.iam.domain.modules.user.model.toPublicInfo
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(Routes.USERS_PATH )
class UserInfoController(
    @Qualifier("authenticationServiceImpl") private val authenticationSrv: AuthenticationOperationService,
    private val userService: UserService
) {
    private val log = customLogger()

    @GetMapping(Routes.USER_INFO_PATH)
    fun getUserInfo(@RequestHeader("Authorization") authHeader: String): ResponseEntity<PublicUserInfo> {
        val token = authHeader.substringAfter("Bearer ")
        val userId = authenticationSrv.getUserIdFromJWT(token)
        userId?.let {
            val user = userService.findById(it)
            return ResponseEntity.ok(user.toPublicInfo())
        }
        throw IllegalArgumentException("User not found")
    }

    @GetMapping(Routes.FIND_USERID_PATH)
    fun findUserId(@RequestHeader("Authorization") authHeader: String,
                   @RequestHeader(Routes.REQ_SOURCE_HEADER)  requestSourceApp: String): ResponseEntity<UUID> {
        log.info("Finding userId for request source: $requestSourceApp")

        if (StringUtils.isBlank(authHeader) || StringUtils.isBlank(requestSourceApp)) {
            throw IllegalArgumentException("What are you doing here? You're not supposed to be here.")
        }

        val token = authHeader.substringAfter("Bearer ")
        val userId = authenticationSrv.getUserIdFromJWT(token)
        return ResponseEntity.ok(userId)
    }
}