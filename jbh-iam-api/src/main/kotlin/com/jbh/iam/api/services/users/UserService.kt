package com.jbh.iam.api.services.users

import com.jbh.iam.api.dto.UserDTO
import com.jbh.iam.core.model.UserCore
import java.util.*

interface UserService {

    //FIXME Check what is DTO vs CORE
    fun findByUniqueUser(userDTO: UserDTO): UserDTO?

    fun existsByNickName(nickName: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findByNickNameOrEmail(nickName: String, email: String): UserCore?

    fun findUserCoreById(id: UUID): UserCore?
}