package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.domain.modules.usergroup.model.UserGroupMembershipEntity
import java.util.*

interface IUserGroupMembershipService {
    fun addMember(groupId: UUID, userId: UUID): UserGroupMembershipEntity
    fun removeMember(groupId: UUID, userId: UUID)
    fun findMembersByGroupId(groupId: UUID): List<UserGroupMembershipEntity>
    fun findGroupsByUserId(userId: UUID): List<UserGroupMembershipEntity>
    fun isMember(groupId: UUID, userId: UUID): Boolean
    fun isAcceptedMember(groupId: UUID, userId: UUID): Boolean

    fun inviteUser(groupId: UUID, targetUserId: UUID, invitedByUserId: UUID): UserGroupMembershipEntity
    fun validateReinvite(groupId: UUID, targetUserId: UUID, invitedByUserId: UUID): UserGroupMembershipEntity
    fun acceptInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity
    fun declineInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity
    fun findPendingInvitationsByUserId(userId: UUID): List<UserGroupMembershipEntity>
    fun findAcceptedMembershipsByUserId(userId: UUID): List<UserGroupMembershipEntity>
    fun findInvitationsByGroupId(groupId: UUID): List<UserGroupMembershipEntity>
}
