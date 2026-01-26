package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.api.dto.toDTOList
import com.jbh.iam.api.services.users.UserServiceImpl
import com.jbh.iam.domain.modules.common.CrudService
import com.jbh.iam.domain.modules.usergroup.model.CreateUserGroupRequest
import com.jbh.iam.domain.modules.usergroup.model.UpdateUserGroupRequest
import com.jbh.iam.domain.modules.usergroup.model.UserGroupDTO
import com.jbh.iam.domain.modules.usergroup.model.UsersGroupEntity
import com.jbh.iam.domain.modules.usergroup.repository.UserGroupRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserGroupServiceImpl(
    private val userGroupRepository: UserGroupRepository,
    private val userService: UserServiceImpl
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
        return userGroupRepository.save(entity)
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
}
