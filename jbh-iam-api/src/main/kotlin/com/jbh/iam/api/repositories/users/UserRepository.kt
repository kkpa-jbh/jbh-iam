package com.jbh.iam.api.repositories.users

import com.jbh.iam.api.domain.Users.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {

    fun findByNickNameOrEmail(nickName: String, email: String): User?

    fun existsByNickName(nickName: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findByNickName(nickName: String): User?
}
