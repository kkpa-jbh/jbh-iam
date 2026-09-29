package com.jbh.iam.api.components.usergroups

import com.jbh.iam.api.Routes
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.domain.modules.usergroup.model.*
import com.jbh.iam.domain.modules.usergroup.service.IInvitationService
import com.jbh.iam.domain.modules.usergroup.service.UserGroupServiceImpl
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Access rules: only an accepted member can read a group and its invitations.
 * Only the owner (`createdBy`) can update or delete a group. See `removeMemberFromGroup` for members.
 */
@RestController
@RequestMapping(Routes.USER_GROUPS_PATH)
class UserGroupController(
    private val userGroupService: UserGroupServiceImpl,
    private val invitationService: IInvitationService,
    private val authenticationOperationService: AuthenticationOperationService
) {

    @GetMapping
    fun getAllUserGroups(): ResponseEntity<List<UserGroupResponse>> {
        authenticationOperationService.requireAdminRole()
        val userGroups = userGroupService.findAllEntities().map { it.toResponse() }
        return ResponseEntity.ok(userGroups)
    }

    @GetMapping("/{id}")
    fun getUserGroupById(
        @PathVariable id: UUID,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<UserGroupResponse> {
        val userGroup = userGroupService.findEntityById(id)
            ?: return ResponseEntity.notFound().build()
        userGroupService.requireMember(id, currentUserId(authHeader))
        return ResponseEntity.ok(userGroup.toResponse())
    }

    @PostMapping
    fun createUserGroup(
        @Valid @RequestBody request: CreateUserGroupRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<UserGroupResponse> {
        val created = userGroupService.create(request, currentUserId(authHeader))
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toResponse())
    }

    @PutMapping("/{id}")
    fun updateUserGroup(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserGroupRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<UserGroupResponse> {
        userGroupService.requireOwner(id, currentUserId(authHeader))
        val updated = userGroupService.updateById(id, request)
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    fun deleteUserGroup(
        @PathVariable id: UUID,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Void> {
        userGroupService.requireOwner(id, currentUserId(authHeader))
        userGroupService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    // Membership of the inviter is checked in UserGroupMembershipServiceImpl.inviteUser.
    @PostMapping("/{groupId}/invitations")
    fun inviteUser(
        @PathVariable groupId: UUID,
        @Valid @RequestBody request: InvitationRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<InvitationResponse> {
        val invitation = invitationService.inviteUserByEmailOrUsername(
            groupId, request, currentUserId(authHeader), authHeader
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation.toInvitationResponse())
    }

    // Membership of the inviter is checked in UserGroupMembershipServiceImpl.validateReinvite.
    @PostMapping("/{groupId}/reinvite")
    fun reinviteUser(
        @PathVariable groupId: UUID,
        @Valid @RequestBody request: InvitationRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<InvitationResponse> {
        val invitation = invitationService.reinviteUserByEmailOrUsername(
            groupId, request, currentUserId(authHeader), authHeader
        )
        return ResponseEntity.ok(invitation.toInvitationResponse())
    }

    @PostMapping("/{groupId}/invitations/accept")
    fun acceptInvitation(
        @PathVariable groupId: UUID,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<InvitationResponse> {
        val membership = invitationService.acceptInvitation(groupId, currentUserId(authHeader))
        return ResponseEntity.ok(membership.toInvitationResponse())
    }

    @PostMapping("/{groupId}/invitations/decline")
    fun declineInvitation(
        @PathVariable groupId: UUID,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<InvitationResponse> {
        val membership = invitationService.declineInvitation(groupId, currentUserId(authHeader))
        return ResponseEntity.ok(membership.toInvitationResponse())
    }

    @GetMapping("/{groupId}/invitations")
    fun getGroupInvitations(
        @PathVariable groupId: UUID,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<List<InvitationResponse>> {
        userGroupService.requireMember(groupId, currentUserId(authHeader))
        val invitations = invitationService.getInvitationsForGroup(groupId)
            .map { it.toInvitationResponse() }
        return ResponseEntity.ok(invitations)
    }

    @GetMapping("/my-invitations")
    fun getMyPendingInvitations(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<List<InvitationResponse>> {
        val invitations = invitationService.getPendingInvitationsForUser(currentUserId(authHeader))
            .map { it.toInvitationResponse() }
        return ResponseEntity.ok(invitations)
    }

    @GetMapping("/my-groups")
    fun getMyGroups(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<List<UserGroupWithMembersResponse>> {
        val groups = userGroupService.findGroupsWithMembersByUserId(currentUserId(authHeader))
        return ResponseEntity.ok(groups)
    }

    @DeleteMapping("/{groupId}/members/{userEmail}")
    fun removeMemberFromGroup(
        @PathVariable groupId: UUID,
        @PathVariable userEmail: String,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Boolean> {
        userGroupService.removeMemberFromGroup(groupId, userEmail, currentUserId(authHeader))
        return ResponseEntity.ok(true)
    }

    private fun currentUserId(authHeader: String): UUID =
        authenticationOperationService.getUserIdFromJWT(authHeader.substringAfter("Bearer "))
            ?: throw IllegalStateException("User ID not found in token")
}
