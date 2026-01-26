package com.jbh.iam.domain.modules.usergroup.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "user_group_membership",
    schema = "iam"
)
class UserGroupMembershipEntity(
    @EmbeddedId
    val id: UserGroupMembershipId = UserGroupMembershipId(),

    @Column(name = "added_at", nullable = false, updatable = false)
    val addedAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: InvitationStatus = InvitationStatus.ACCEPTED,

    @Column(name = "invited_at")
    val invitedAt: LocalDateTime? = null,

    @Column(name = "accepted_at")
    var acceptedAt: LocalDateTime? = null,

    @Column(name = "declined_at")
    var declinedAt: LocalDateTime? = null
) {
    constructor(groupId: UUID, userId: UUID, status: InvitationStatus = InvitationStatus.ACCEPTED) : this(
        id = UserGroupMembershipId(groupId = groupId, userId = userId),
        status = status,
        invitedAt = LocalDateTime.now(),
        acceptedAt = if (status == InvitationStatus.ACCEPTED) LocalDateTime.now() else null
    )
}
