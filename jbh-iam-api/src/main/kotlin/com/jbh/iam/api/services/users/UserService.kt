package com.jbh.iam.api.services.users

import com.jbh.iam.api.dto.InternalUserDTO
import java.util.*

interface UserService {

    fun findByUniqueUser(userDTO: InternalUserDTO): InternalUserDTO

    fun existsByNickName(nickName: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findByNickNameOrEmail(nickName: String, email: String): InternalUserDTO

    fun findById(id: UUID): InternalUserDTO
}