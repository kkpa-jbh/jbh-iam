package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.common.logging.customLogger
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
            throw IllegalArgumentException("User $userId is already a member of group $groupId")
        }

        val membership = UserGroupMembershipEntity(groupId = groupId, userId = userId, status = InvitationStatus.ACCEPTED)
        return membershipRepository.save(membership)
    }

    override fun removeMember(groupId: UUID, userId: UUID) {
        log.info("Removing user $userId from group $groupId")

        val membershipId = UserGroupMembershipId(groupId = groupId, userId = userId)
        if (!membershipRepository.existsById(membershipId)) {
            throw IllegalArgumentException("User $userId is not a member of group $groupId")
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
            throw IllegalArgumentException("User $invitedByUserId is not an accepted member of group $groupId and cannot invite others")
        }

        if (membershipRepository.existsByIdGroupIdAndIdUserId(groupId, targetUserId)) {
            throw IllegalArgumentException("User $targetUserId already has a membership record for group $groupId")
        }

        val invitation = UserGroupMembershipEntity(
            groupId = groupId,
            userId = targetUserId,
            status = InvitationStatus.PENDING_TO_ACCEPT
        )
        return membershipRepository.save(invitation)
    }

    override fun acceptInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity {
        log.info("User $userId accepting invitation to group $groupId")

        val membership = membershipRepository.findByIdGroupIdAndIdUserId(groupId, userId)
            ?: throw IllegalArgumentException("No invitation found for user $userId in group $groupId")

        if (membership.status != InvitationStatus.PENDING_TO_ACCEPT) {
            throw IllegalArgumentException("Invitation is not pending. Current status: ${membership.status}")
        }

        membership.status = InvitationStatus.ACCEPTED
        membership.acceptedAt = LocalDateTime.now()
        return membershipRepository.save(membership)
    }

    override fun declineInvitation(groupId: UUID, userId: UUID): UserGroupMembershipEntity {
        log.info("User $userId declining invitation to group $groupId")

        val membership = membershipRepository.findByIdGroupIdAndIdUserId(groupId, userId)
            ?: throw IllegalArgumentException("No invitation found for user $userId in group $groupId")

        if (membership.status != InvitationStatus.PENDING_TO_ACCEPT) {
            throw IllegalArgumentException("Invitation is not pending. Current status: ${membership.status}")
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
