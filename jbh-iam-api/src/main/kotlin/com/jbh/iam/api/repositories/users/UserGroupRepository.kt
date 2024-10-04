package com.jbh.iam.api.repositories.users

import com.jbh.iam.api.domain.Users.UsersGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserGroupRepository : JpaRepository<UsersGroup, UUID> {

}
