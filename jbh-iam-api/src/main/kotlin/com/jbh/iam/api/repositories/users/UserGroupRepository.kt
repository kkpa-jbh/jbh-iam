package com.jbh.iam.api.repositories.users

import com.jbh.iam.api.domain.Users.User
import com.jbh.iam.api.domain.Users.UsersGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserGroupRepository : JpaRepository<UsersGroup, UUID> {

    fun findByUserOwnerAndSingle(userOwner: User, single: Boolean): UsersGroup?

    fun findByNameAndSingle(name: String, single: Boolean): UsersGroup?

    fun findByUserOwnerNickNameOrUserOwnerEmail(nickName: String, email: String): UsersGroup?

    fun findByUserOwnerId(id: UUID): UsersGroup?
}
