package com.jbh.iam.api.services.users

import com.jbh.iam.domain.modules.user.model.InternalUserDTO
import com.jbh.iam.domain.modules.user.model.UserEntity
import java.util.*

interface UserService {

    fun existsByNickName(nickName: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findByNickNameOrEmail(nickName: String, email: String): InternalUserDTO

    fun findById(id: UUID): InternalUserDTO
    fun findByEmail(email: String): InternalUserDTO

    fun findAllByIds(ids:  List<UUID>) : List<InternalUserDTO>

    fun save(userDomain: UserEntity): InternalUserDTO
    fun fetchByUserNameOrEmail(userNameOrEmail: String): InternalUserDTO

}