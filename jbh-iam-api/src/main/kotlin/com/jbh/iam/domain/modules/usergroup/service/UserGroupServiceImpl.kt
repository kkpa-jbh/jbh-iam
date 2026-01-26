package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.api.dto.toDTOList
import com.jbh.iam.api.services.users.UserServiceImpl
import com.jbh.iam.domain.modules.common.CrudService
import com.jbh.iam.domain.modules.user.repository.UserRepository
import com.jbh.iam.domain.modules.usergroup.model.*
import com.jbh.iam.domain.modules.usergroup.repository.UserGroupRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserGroupServiceImpl(
    private val userGroupRepository: UserGroupRepository,
    private val userRepository: UserRepository,
    private val userService: UserServiceImpl,
    private val membershipService: IUserGroupMembershipService
) : CrudService<UserGroupDTO>(), IUserGroupService {

    override fun validateSaveOperation(dto: UserGroupDTO) {
    }

    override fun validateUpdateOperation(dto: UserGroupDTO) {
    }

    override fun updateOperation(dto: UserGroupDTO): UserGroupDTO {
        return userGroupRepository.save(dto.toDomain()).toDTO()
    }

    override fun delete(dto: UserGroupDTO): Boolean {
        dto.also {
            try {
                if (dto.id == null) throw IllegalArgumentException("There isn't a user group defined to be removed")
                userGroupRepository.deleteById(dto.id!!)
                return true
            } catch (enf: Exception) {
                throw IllegalArgumentException("User group $it not found")
            }
        }
        throw IllegalArgumentException("No user group to be deleted")
    }

    override fun findAll(): List<UserGroupDTO> {
        return userGroupRepository.findAll().toDTOList()
    }

    override fun saveOperation(userGroupDTO: UserGroupDTO): UserGroupDTO {
        log.info("Saving UserGroup $userGroupDTO")
        val userGroupDomain = userGroupDTO.toDomain()
        val userGroupCreated = userGroupRepository.save(userGroupDomain)
        return userGroupCreated.toDTO()
    }

    override fun findById(id: UUID): UserGroupDTO? = userGroupRepository.findById(id).orElse(null)?.toDTO()

    override fun isNewAndUnique(dto: UserGroupDTO): Boolean {
        if (dto.id == null) {
            return false
        }
        return !exists(dto)
    }

    private fun exists(dto: UserGroupDTO): Boolean {
        val categoryDTO = findById(dto.id!!)
        categoryDTO?.let {
            return true
        }
        return false
    }

    override fun create(request: CreateUserGroupRequest, createdByUserId: UUID): UsersGroupEntity {
        log.info("Creating UserGroup with shortName=${request.shortName}, name=${request.name}, createdBy=$createdByUserId")
        val entity = UsersGroupEntity(
            shortName = request.shortName,
            name = request.name,
            createdBy = createdByUserId
        )
        val savedGroup = userGroupRepository.save(entity)

        membershipService.addMember(groupId = savedGroup.id!!, userId = createdByUserId)
        log.info("Added creator $createdByUserId as member of group ${savedGroup.id}")

        return savedGroup
    }

    override fun updateById(id: UUID, request: UpdateUserGroupRequest): UsersGroupEntity {
        val existing = userGroupRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User group with id $id not found") }

        val updated = UsersGroupEntity(
            id = existing.id,
            shortName = request.shortName ?: existing.shortName,
            name = request.name ?: existing.name,
            createdBy = existing.createdBy
        ).apply {
            createdAt = existing.createdAt
        }

        return userGroupRepository.save(updated)
    }

    override fun deleteById(id: UUID) {
        if (!userGroupRepository.existsById(id)) {
            throw IllegalArgumentException("User group with id $id not found")
        }
        userGroupRepository.deleteById(id)
    }

    override fun findEntityById(id: UUID): UsersGroupEntity? {
        return userGroupRepository.findById(id).orElse(null)
    }

    override fun findAllEntities(): List<UsersGroupEntity> {
        return userGroupRepository.findAll()
    }

    override fun findGroupsByUserId(userId: UUID): List<UsersGroupEntity> {
        val acceptedMemberships = membershipService.findAcceptedMembershipsByUserId(userId)
        val groupIds = acceptedMemberships.map { it.id.groupId }
        return userGroupRepository.findAllById(groupIds)
    }

    override fun findGroupsWithMembersByUserId(userId: UUID): List<UserGroupWithMembersResponse> {
        val groups = findGroupsByUserId(userId)

        return groups.map { group ->
            val memberships = membershipService.findMembersByGroupId(group.id!!)
            val userIds = memberships.map { it.id.userId }
            val users = userRepository.findAllById(userIds).associateBy { it.id }

            val members = memberships.mapNotNull { membership ->
                val user = users[membership.id.userId]
                user?.let {
                    GroupMemberResponse(
                        nickName = it.nickName,
                        email = it.email,
                        status = membership.status,
                        isGroupOwner = it.id == group.createdBy
                    )
                }
            }

            UserGroupWithMembersResponse(
                id = group.id!!,
                shortName = group.shortName,
                name = group.name,
                createdBy = group.createdBy,
                createdAt = group.createdAt,
                updatedAt = group.updatedAt,
                members = members
            )
        }
    }
}
