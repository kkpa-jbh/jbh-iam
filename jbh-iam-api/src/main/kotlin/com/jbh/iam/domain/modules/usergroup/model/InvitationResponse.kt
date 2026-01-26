package com.jbh.iam.domain.modules.usergroup.model

import java.time.LocalDateTime
import java.util.*

data class InvitationResponse(
    val groupId: UUID,
    val userId: UUID,
    val status: InvitationStatus,
    val invitedAt: LocalDateTime?,
    val acceptedAt: LocalDateTime?,
    val declinedAt: LocalDateTime?
)

fun UserGroupMembershipEntity.toInvitationResponse(): InvitationResponse {
    return InvitationResponse(
        groupId = this.id.groupId,
        userId = this.id.userId,
        status = this.status,
        invitedAt = this.invitedAt,
        acceptedAt = this.acceptedAt,
        declinedAt = this.declinedAt
    )
}
