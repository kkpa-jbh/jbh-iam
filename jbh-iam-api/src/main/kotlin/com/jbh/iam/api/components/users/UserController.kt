package com.jbh.iam.api.components.users

import com.jbh.iam.api.Routes
import com.jbh.iam.api.services.users.UserServiceImpl
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.domain.modules.user.model.PublicUserInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Admin only: the answers contain emails of other users.
 * A normal user reads their own data with `GET /users/userinfo` (UserInfoController).
 */
@RestController
@RequestMapping(Routes.USERS_PATH)
class UserController(
    private val userServiceImpl: UserServiceImpl,
    private val authenticationOperationService: AuthenticationOperationService
) {

    @GetMapping("/{nickName}")
    fun getUserProfile(@PathVariable(value = "nickName") nickName: String): ResponseEntity<PublicUserInfo> {
        authenticationOperationService.requireAdminRole()
        val user = userServiceImpl.findByNickName(nickName)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/")
    fun findAll(): List<PublicUserInfo> {
        authenticationOperationService.requireAdminRole()
        return userServiceImpl.findAll()
    }
}
