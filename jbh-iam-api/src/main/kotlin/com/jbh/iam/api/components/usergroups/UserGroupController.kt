package com.jbh.iam.api.components.usergroups

import com.jbh.iam.api.Routes
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.domain.modules.usergroup.model.CreateUserGroupRequest
import com.jbh.iam.domain.modules.usergroup.model.UpdateUserGroupRequest
import com.jbh.iam.domain.modules.usergroup.model.UserGroupResponse
import com.jbh.iam.domain.modules.usergroup.model.toResponse
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
}
