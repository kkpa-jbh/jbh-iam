package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.api.exceptions.IAMApiExceptionType
import com.jbh.iam.common.exceptions.BusinessException
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.domain.modules.user.model.InternalUserDTO
import com.jbh.iam.domain.modules.usergroup.model.InvitationStatus
import com.jbh.iam.domain.modules.usergroup.model.UserGroupMembershipEntity
import com.jbh.iam.domain.modules.usergroup.model.UserGroupMembershipId
import com.jbh.iam.domain.modules.usergroup.repository.UserGroupMembershipRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class UserGroupMembershipServiceImpl(
    private val membershipRepository: UserGroupMembershipRepository
) : IUserGroupMembershipService {

    private val log = customLogger()

    override fun addMember(groupId: UUID, userId: UUID): UserGroupMembershipEntity {
        log.info("Adding user $userId to group $groupId")

        if (membershipRepository.existsByIdGroupIdAndIdUserId(groupId, userId)) {
            throw BusinessException(IAMApiExceptionType.USER_ALREADY_MEMBER, userId, groupId)
        }

        val membership = UserGroupMembershipEntity(groupId = groupId, userId = userId, status = InvitationStatus.ACCEPTED)
        return membershipRepository.save(membership)
    }

    override fun removeMember(groupId: UUID, userId: UUID) {
        log.info("Removing user $userId from group $groupId")

        val membershipId = UserGroupMembershipId(groupId = groupId, userId = userId)
        if (!membershipRepository.existsById(membershipId)) {
            throw BusinessException(IAMApiExceptionType.USER_NOT_MEMBER, userId, groupId)
        }

        membershipRepository.deleteById(membershipId)
    }

    override fun findMembersByGroupId(groupId: UUID): List<UserGroupMembershipEntity> {
        return membershipRepository.findByIdGroupId(groupId)
    }

    override fun findGroupsByUserId(userId: UUID): List<UserGroupMembershipEntity> {
        return membershipRepository.findByIdUserId(userId)
    }

    override fun isMember(groupId: UUID, userId: UUID): Boolean {
        return membershipRepository.existsByIdGroupIdAndIdUserId(groupId, userId)
    }

    override fun isAcceptedMember(groupId: UUID, userId: UUID): Boolean {
        return membershipRepository.existsByIdGroupIdAndIdUserIdAndStatus(groupId, userId, InvitationStatus.ACCEPTED)
    }

    override fun inviteUser(groupId: UUID, targetUserId: UUID, invitedByUserId: UUID): UserGroupMembershipEntity {
        log.info("User $invitedByUserId inviting user $targetUserId to group $groupId")

        if (!isAcceptedMember(groupId, invitedByUserId)) {
            throw BusinessException(IAMApiExceptionType.USER_NOT_ACCEPTED_MEMBER, invitedByUserId, groupId)
        }

        if (membershipRepository.existsByIdGroupIdAndIdUserId(groupId, targetUserId)) {
            throw BusinessException(IAMApiExceptionType.USER_ALREADY_HAS_MEMBERSHIP, targetUserId, groupId)
        }

        val invitation = UserGroupMembershipEntity(
            groupId = groupId,
            userId = targetUserId,
            status = InvitationStatus.PENDING_TO_ACCEPT
        )
        return membershipRepository.save(invitation)
    }

    override fun validateReinvite(groupId: UUID, targetUserId: UUID, invitedByUserId: UUID): UserGroupMembershipEntity {
        log.info("User $invitedByUserId reinviting user $targetUserId to group $groupId")

        if (!isAcceptedMember(groupId, invitedByUserId)) {
            throw BusinessException(IAMApiExceptionType.USER_NOT_ACCEPTED_MEMBER, invitedByUserId, groupId)
        }

        val membership = membershipRepository.findByIdGroupIdAndIdUserId(groupId, targetUserId)
            ?: throw BusinessException(IAMApiExceptionType.NO_INVITATION_FOUND, targetUserId, groupId)

        if (membership.status != InvitationStatus.PENDING_TO_ACCEPT) {
            throw BusinessException(IAMApiExceptionType.INVITATION_NOT_PENDING, membership.status)
        }

        return membership
    }

    /**
     *  The page handles these error scenarios:
     *   - 404: Invitation not found (revoked or
     *   already used)
     *   - 409: User is already a member
     *   - 410: Invitation expired
     *   - Generic errors: Fallback message with
     *   helpful tips
     */
    override fun acceptInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity {
        log.info("User $userId accepting invitation to group $groupId")

        val membership = membershipRepository.findByIdGroupIdAndIdUserId(groupId, userId)
            ?: throw BusinessException(IAMApiExceptionType.NO_INVITATION_FOUND, userId, groupId)

        if (membership.status != InvitationStatus.PENDING_TO_ACCEPT) {
            throw BusinessException(IAMApiExceptionType.INVITATION_NOT_PENDING, membership.status)
        }

        membership.status = InvitationStatus.ACCEPTED
        membership.acceptedAt = LocalDateTime.now()
        return membershipRepository.save(membership)
    }

    override fun declineInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity {
        log.info("User $userId declining invitation to group $groupId")

        val membership = membershipRepository.findByIdGroupIdAndIdUserId(groupId, userId)
            ?: throw BusinessException(IAMApiExceptionType.NO_INVITATION_FOUND, userId, groupId)

        if (membership.status != InvitationStatus.PENDING_TO_ACCEPT) {
            throw BusinessException(IAMApiExceptionType.INVITATION_NOT_PENDING, membership.status)
        }

        membership.status = InvitationStatus.DECLINED
        membership.declinedAt = LocalDateTime.now()
        return membershipRepository.save(membership)
    }

    override fun findPendingInvitationsByUserId(userId: UUID): List<UserGroupMembershipEntity> {
        return membershipRepository.findByIdUserIdAndStatus(userId, InvitationStatus.PENDING_TO_ACCEPT)
    }

    override fun findAcceptedMembershipsByUserId(userId: UUID): List<UserGroupMembershipEntity> {
        return membershipRepository.findByIdUserIdAndStatus(userId, InvitationStatus.ACCEPTED)
    }

    override fun findInvitationsByGroupId(groupId: UUID): List<UserGroupMembershipEntity> {
        return membershipRepository.findByIdGroupId(groupId)
    }
}
