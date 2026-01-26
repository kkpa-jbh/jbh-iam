package com.jbh.iam.domain.modules.usergroup.model

import java.time.LocalDateTime
import java.util.*

data class UserGroupResponse(
    val id: UUID,
    val shortName: String,
    val name: String,
    val createdBy: UUID?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)

fun UsersGroupEntity.toResponse(): UserGroupResponse {
    return UserGroupResponse(
        id = this.id!!,
        shortName = this.shortName,
        name = this.name,
        createdBy = this.createdBy,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun UserGroupDTO.toResponse(createdAt: LocalDateTime, updatedAt: LocalDateTime?): UserGroupResponse {
    return UserGroupResponse(
        id = this.id!!,
        shortName = this.shortName,
        name = this.name,
        createdBy = this.createdBy,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
