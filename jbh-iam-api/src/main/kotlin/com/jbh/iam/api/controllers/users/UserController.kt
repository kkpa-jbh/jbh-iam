package com.jbh.iam.api.controllers.users

import com.jbh.iam.api.Routes
import com.jbh.iam.api.dto.UserDTO
import com.jbh.iam.api.services.users.UserServiceImpl
import com.jbh.iam.common.payload.UserProfileResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneOffset

@RestController
@RequestMapping(Routes.USERS_PATH)
class UserController(private val userServiceImpl: UserServiceImpl) {

    @GetMapping("/{nickName}")
    fun getUserProfile(@PathVariable(value = "nickName") nickName: String): ResponseEntity<UserProfileResponse> {
        val user = userServiceImpl.findByNickName(nickName)

        val userProfile = UserProfileResponse(
            name = user.userName, id = user.id!!, username = user.nickName, joinedAt = user.createdAt!!.toInstant(
                ZoneOffset.UTC
            )
        )

        return ResponseEntity.ok(userProfile)
    }

    @GetMapping("/")
    fun findAll(): List<UserDTO> = userServiceImpl.findAll()
}