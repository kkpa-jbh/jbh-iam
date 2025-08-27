package com.jbh.iam.domain.modules.usergroup.repository

import com.jbh.iam.domain.modules.usergroup.model.UsersGroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserGroupRepository : JpaRepository<UsersGroupEntity, UUID> {

}
