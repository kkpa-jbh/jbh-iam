package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.domain.modules.usergroup.model.CreateUserGroupRequest
import com.jbh.iam.domain.modules.usergroup.model.UpdateUserGroupRequest
import com.jbh.iam.domain.modules.usergroup.model.UsersGroupEntity
import java.util.*

interface IUserGroupService {
    fun create(request: CreateUserGroupRequest, createdByUserId: UUID): UsersGroupEntity
    fun updateById(id: UUID, request: UpdateUserGroupRequest): UsersGroupEntity
    fun deleteById(id: UUID)
    fun findEntityById(id: UUID): UsersGroupEntity?
    fun findAllEntities(): List<UsersGroupEntity>
}