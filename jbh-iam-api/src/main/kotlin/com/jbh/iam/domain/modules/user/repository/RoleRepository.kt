package com.jbh.iam.domain.modules.user.repository

import com.jbh.iam.core.model.RoleName
import com.jbh.iam.domain.modules.user.model.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : JpaRepository<RoleEntity, UUID> {
    fun findByName(roleName: RoleName): Optional<RoleEntity>
}