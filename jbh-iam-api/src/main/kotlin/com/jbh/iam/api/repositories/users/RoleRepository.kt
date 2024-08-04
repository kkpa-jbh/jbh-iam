package com.jbh.iam.api.repositories.users

import com.jbh.iam.api.domain.Users.Role
import com.jbh.iam.core.model.RoleName
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : JpaRepository<Role, UUID> {
    fun findByName(roleName: RoleName): Optional<Role>
}