package com.jbh.iam.domain.usergroup.repository

import com.jbh.iam.domain.usergroup.UsersGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserGroupRepository : JpaRepository<UsersGroup, UUID> {

}
