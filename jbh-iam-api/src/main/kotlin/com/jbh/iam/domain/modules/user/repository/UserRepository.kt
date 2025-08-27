package com.jbh.iam.domain.modules.user.repository

import com.jbh.iam.domain.modules.user.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {

    fun findByNickNameOrEmail(nickName: String, email: String): UserEntity?

    fun existsByNickName(nickName: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findByNickName(nickName: String): UserEntity?
}
