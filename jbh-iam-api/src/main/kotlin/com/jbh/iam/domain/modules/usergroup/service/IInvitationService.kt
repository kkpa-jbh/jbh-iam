package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.domain.modules.usergroup.model.InvitationRequest
import com.jbh.iam.domain.modules.usergroup.model.UserGroupMembershipEntity
import java.util.*

interface IInvitationService {
    fun inviteUserByEmailOrUsername(groupId: UUID, request: InvitationRequest, invitedByUserId: UUID, token: String): UserGroupMembershipEntity
    fun reinviteUserByEmailOrUsername(groupId: UUID, request:  InvitationRequest, invitedByUserId: UUID, token: String): UserGroupMembershipEntity
    fun acceptInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity
    fun declineInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity
    fun getPendingInvitationsForUser(userId: UUID): List<UserGroupMembershipEntity>
    fun getInvitationsForGroup(groupId: UUID): List<UserGroupMembershipEntity>
}
