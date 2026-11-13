package com.jbh.iam.domain.modules.user.service

import com.jbh.iam.api.exceptions.web.AppException
import com.jbh.iam.core.model.RoleName
import com.jbh.iam.domain.modules.user.model.RoleEntity
import com.jbh.iam.domain.modules.user.repository.RoleRepository
import org.springframework.stereotype.Service

/** Finds the roles that sign-up and Google sign-in give to a new user. */
@Service
class RoleService(private val roleRepository: RoleRepository) {

    fun defaultUserRole(): RoleEntity =
        roleRepository.findByName(RoleName.ROLE_USER).orElseThrow { AppException("User Role not set.") }
}
