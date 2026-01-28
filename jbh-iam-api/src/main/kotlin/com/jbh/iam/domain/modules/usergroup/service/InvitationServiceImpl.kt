package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.api.services.users.UserServiceImpl
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.domain.modules.usergroup.model.InvitationRequest
import com.jbh.iam.domain.modules.usergroup.model.UserGroupMembershipEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class InvitationServiceImpl(
    private val userService: UserServiceImpl,
    private val membershipService: IUserGroupMembershipService,
    private val notificationService: IInvitationNotificationService
) : IInvitationService {

    private val log = customLogger()

    override fun inviteUserByEmailOrUsername(
        groupId: UUID,
        invitationRequest: InvitationRequest,
        invitedByUserId: UUID,
        token: String
    ): UserGroupMembershipEntity {
        val emailOrUsername = invitationRequest.emailOrUsername

        log.info("Inviting user with emailOrUsername=$emailOrUsername to group $groupId by user $invitedByUserId")

        val targetUser = userService.findByNickNameOrEmail(emailOrUsername, emailOrUsername)
            ?: throw IllegalArgumentException("User not found with email or username: $emailOrUsername")

        val invitedByUser = userService.findById(invitedByUserId)

        val targetUserId = targetUser.id
            ?: throw IllegalStateException("User found but has no ID")

        val membership =  membershipService.inviteUser(groupId, targetUserId, invitedByUserId)


        notificationService.sendInvitationNotification(invitationRequest, targetUser, invitedByUser, token)
        return membership
    }

    override fun reinviteUserByEmailOrUsername(
        groupId: UUID,
        emailOrUsername: String,
        invitedByUserId: UUID
    ): UserGroupMembershipEntity {
        log.info("Reinviting user with emailOrUsername=$emailOrUsername to group $groupId by user $invitedByUserId")

        val targetUser = userService.findByNickNameOrEmail(emailOrUsername, emailOrUsername)
            ?: throw IllegalArgumentException("User not found with email or username: $emailOrUsername")

        val targetUserId = targetUser.id
            ?: throw IllegalStateException("User found but has no ID")

        val membership = membershipService.validateReinvite(groupId, targetUserId, invitedByUserId)
        notificationService.sendReinviteNotification(groupId, targetUserId, invitedByUserId)
        return membership
    }

    override fun acceptInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity {
        val membership =  membershipService.acceptInvitation(groupId, userId)
        return membership;
    }

    override fun declineInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity {
        return membershipService.declineInvitation(groupId, userId)
    }

    override fun getPendingInvitationsForUser(userId: UUID): List<UserGroupMembershipEntity> {
        return membershipService.findPendingInvitationsByUserId(userId)
    }

    override fun getInvitationsForGroup(groupId: UUID): List<UserGroupMembershipEntity> {
        return membershipService.findInvitationsByGroupId(groupId)
    }
}
