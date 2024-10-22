package com.jbh.iam.api.controllers.users

import com.jbh.iam.api.Routes
import com.jbh.iam.api.dto.PublicUserInfo
import com.jbh.iam.api.services.users.UserServiceImpl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Routes.USERS_PATH)
class UserController(private val userServiceImpl: UserServiceImpl) {

    @GetMapping("/{nickName}")
    fun getUserProfile(@PathVariable(value = "nickName") nickName: String): ResponseEntity<PublicUserInfo> {
        val user = userServiceImpl.findByNickName(nickName)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/")
    fun findAll(): List<PublicUserInfo> = userServiceImpl.findAll()
}