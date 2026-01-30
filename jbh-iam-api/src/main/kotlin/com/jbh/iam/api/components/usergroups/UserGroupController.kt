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
    fun getUserGroupById(@PathVariable id: UUID): ResponseEntity<UserGroupResponse> {
        val userGroup = userGroupService.findEntityById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(userGroup.toResponse())
    }

    @PostMapping
    fun createUserGroup(
        @Valid @RequestBody request: CreateUserGroupRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<UserGroupResponse> {
        val token = authHeader.substringAfter("Bearer ")
        val userId = authenticationOperationService.getUserIdFromJWT(token)
            ?: throw IllegalStateException("User ID not found in token")
        val created = userGroupService.create(request, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toResponse())
    }

    @PutMapping("/{id}")
    fun updateUserGroup(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserGroupRequest
    ): ResponseEntity<UserGroupResponse> {
        val updated = userGroupService.updateById(id, request)
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    fun deleteUserGroup(@PathVariable id: UUID): ResponseEntity<Void> {
        userGroupService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{groupId}/invitations")
    fun inviteUser(
        @PathVariable groupId: UUID,
        @Valid @RequestBody request: InvitationRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<InvitationResponse> {
        val token = authHeader.substringAfter("Bearer ")
        val invitedByUserId = authenticationOperationService.getUserIdFromJWT(token)
            ?: throw IllegalStateException("User ID not found in token")

        val enrichedRequest = request.copy( teamID = groupId)

        val invitation = invitationService.inviteUserByEmailOrUsername(groupId, enrichedRequest, invitedByUserId, authHeader)
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation.toInvitationResponse())
    }

    @PostMapping("/{groupId}/reinvite")
    fun reinviteUser(
        @PathVariable groupId: UUID,
        @Valid @RequestBody request: InvitationRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<InvitationResponse> {
        val token = authHeader.substringAfter("Bearer ")
        val invitedByUserId = authenticationOperationService.getUserIdFromJWT(token)
            ?: throw IllegalStateException("User ID not found in token")

        val invitation = invitationService.reinviteUserByEmailOrUsername(groupId, request.emailOrUsername, invitedByUserId)
        return ResponseEntity.ok(invitation.toInvitationResponse())
    }

    @PostMapping("/{groupId}/invitations/accept")
    fun acceptInvitation(
        @PathVariable groupId: UUID,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<InvitationResponse> {
        val token = authHeader.substringAfter("Bearer ")
        val userId = authenticationOperationService.getUserIdFromJWT(token)
            ?: throw IllegalStateException("User ID not found in token")

        val membership = invitationService.acceptInvitation(groupId, userId)
        return ResponseEntity.ok(membership.toInvitationResponse())
    }

    @PostMapping("/{groupId}/invitations/decline")
    fun declineInvitation(
        @PathVariable groupId: UUID,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<InvitationResponse> {
        val token = authHeader.substringAfter("Bearer ")
        val userId = authenticationOperationService.getUserIdFromJWT(token)
            ?: throw IllegalStateException("User ID not found in token")

        val membership = invitationService.declineInvitation(groupId, userId)
        return ResponseEntity.ok(membership.toInvitationResponse())
    }

    @GetMapping("/{groupId}/invitations")
    fun getGroupInvitations(@PathVariable groupId: UUID): ResponseEntity<List<InvitationResponse>> {
        val invitations = invitationService.getInvitationsForGroup(groupId)
            .map { it.toInvitationResponse() }
        return ResponseEntity.ok(invitations)
    }

    @GetMapping("/my-invitations")
    fun getMyPendingInvitations(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<List<InvitationResponse>> {
        val token = authHeader.substringAfter("Bearer ")
        val userId = authenticationOperationService.getUserIdFromJWT(token)
            ?: throw IllegalStateException("User ID not found in token")

        val invitations = invitationService.getPendingInvitationsForUser(userId)
            .map { it.toInvitationResponse() }
        return ResponseEntity.ok(invitations)
    }

    @GetMapping("/my-groups")
    fun getMyGroups(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<List<UserGroupWithMembersResponse>> {
        val token = authHeader.substringAfter("Bearer ")
        val userId = authenticationOperationService.getUserIdFromJWT(token)
            ?: throw IllegalStateException("User ID not found in token")

        val groups = userGroupService.findGroupsWithMembersByUserId(userId)
        return ResponseEntity.ok(groups)
    }

    @DeleteMapping("/{groupId}/members/{userEmail}")
    fun removeMemberFromGroup(@PathVariable groupId: UUID,
                              @PathVariable userEmail: String,
                              @RequestHeader("Authorization") authHeader: String): ResponseEntity<Boolean> {
        userGroupService.removeMemberFromGroup(groupId, userEmail)
        return ResponseEntity.ok(true)
    }
}
