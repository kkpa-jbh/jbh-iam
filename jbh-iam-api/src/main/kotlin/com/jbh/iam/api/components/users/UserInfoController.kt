package com.jbh.iam.api.components.users

import com.jbh.iam.api.Routes
import com.jbh.iam.api.services.users.UserService
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.domain.modules.user.model.PublicUserInfo
import com.jbh.iam.domain.modules.user.model.toPublicInfo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Routes.USER_INFO_PATH)
class UserInfoController(
    @Qualifier("authenticationServiceImpl") private val authenticationSrv: AuthenticationOperationService,
    private val userService: UserService
) {

    @GetMapping("")
    fun getUserInfo(@RequestHeader("Authorization") authHeader: String): ResponseEntity<PublicUserInfo> {
        val token = authHeader.substringAfter("Bearer ")
        val userId = authenticationSrv.getUserIdFromJWT(token)
        userId?.let {
            val user = userService.findById(it)
            return ResponseEntity.ok(user.toPublicInfo())
        }
        throw IllegalArgumentException("User not found")
    }
}