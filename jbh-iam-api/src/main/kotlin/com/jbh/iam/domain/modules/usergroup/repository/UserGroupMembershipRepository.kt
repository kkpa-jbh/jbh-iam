package com.jbh.iam.domain.modules.usergroup.repository

import com.jbh.iam.domain.modules.usergroup.model.InvitationStatus
import com.jbh.iam.domain.modules.usergroup.model.UserGroupMembershipEntity
import com.jbh.iam.domain.modules.usergroup.model.UserGroupMembershipId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserGroupMembershipRepository : JpaRepository<UserGroupMembershipEntity, UserGroupMembershipId> {
    fun findByIdGroupId(groupId: UUID): List<UserGroupMembershipEntity>
    fun findByIdUserId(userId: UUID): List<UserGroupMembershipEntity>
    fun findByIdUserIdAndStatus(userId: UUID, status: InvitationStatus): List<UserGroupMembershipEntity>
    fun findByIdGroupIdAndStatus(groupId: UUID, status: InvitationStatus): List<UserGroupMembershipEntity>
    fun findByIdGroupIdAndIdUserId(groupId: UUID, userId: UUID): UserGroupMembershipEntity?
    fun existsByIdGroupIdAndIdUserId(groupId: UUID, userId: UUID): Boolean
    fun existsByIdGroupIdAndIdUserIdAndStatus(groupId: UUID, userId: UUID, status: InvitationStatus): Boolean
    fun deleteByIdGroupId(groupId: UUID)
}
