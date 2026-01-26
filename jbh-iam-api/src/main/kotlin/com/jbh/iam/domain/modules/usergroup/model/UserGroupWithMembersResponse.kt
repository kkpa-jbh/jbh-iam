package com.jbh.iam.domain.modules.usergroup.model

import java.time.LocalDateTime
import java.util.*

data class UserGroupWithMembersResponse(
    val id: UUID,
    val shortName: String,
    val name: String,
    val createdBy: UUID?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val members: List<GroupMemberResponse>
)
