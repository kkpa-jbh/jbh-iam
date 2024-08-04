package com.jbh.iam.api.services.users

import com.jbh.iam.api.domain.Users.UsersGroup
import com.jbh.iam.api.domain.toDTOList
import com.jbh.iam.api.dto.UserGroupDTO
import com.jbh.iam.api.repositories.users.UserGroupRepository
import com.jbh.iam.api.services.CrudService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserGroupServiceImpl(
    private val userGroupRepository: UserGroupRepository,
    private val userService: UserServiceImpl
) : CrudService<UserGroupDTO>(), UserGroupService {

    override fun validateSaveOperation(dto: UserGroupDTO) {
        when (dto.single) {
            true -> {
                if (userAlreadyHaveASingleGroup(dto)) {
                    throw IllegalAccessException("User already have a single group created")
                }
            }

            false -> {
                if (userAlreadyHaveASingleGroup(dto).not()) {
                    throw IllegalAccessException("User doesn't have a single group created")
                }
            }
        }
    }

    private fun userAlreadyHaveASingleGroup(userGroupDTO: UserGroupDTO): Boolean {
        val userGroup = findByUserOwnerAndSingle(userGroupDTO)

        return (userGroup != null)
    }

    override fun validateUpdateOperation(dto: UserGroupDTO) {
    }

    override fun updateOperation(dto: UserGroupDTO): UserGroupDTO {
        val userUpdatedDTO = dto.userOwner?.let { userService.update(it) }
        dto.userOwner = userUpdatedDTO

        return userGroupRepository.save(dto.toDomain()).toDTO()
    }

    override fun delete(dto: UserGroupDTO): Boolean {
        dto.also {
            try {
                if (dto.id == null) throw IllegalArgumentException("There isn't a user groupd defined to be removed")

                userGroupRepository.deleteById(dto.id!!); return true
            } catch (enf: Exception) {
                throw IllegalArgumentException("Category $it not found")
            }
        }

        throw IllegalArgumentException("Not category to be deleted")
    }

    override fun findAll(): List<UserGroupDTO> {
        return userGroupRepository.findAll().toDTOList()
    }

    override fun saveOperation(userGroupDTO: UserGroupDTO): UserGroupDTO {
        val userOwnerDTO = userGroupDTO.userOwner
        if (userOwnerDTO?.id == null) {
            log.info("Saving User: ${userGroupDTO.userOwner?.email}")
            if (userOwnerDTO != null) {
                userService.save(userOwnerDTO).let {
                    userGroupDTO.userOwner = it
                }
            }
        }

        log.info("Saving UserGroup $userGroupDTO")
        val userGroupDomain = userGroupDTO.toDomain()
        val userGroupCreated = userGroupRepository.save(userGroupDomain)
        return userGroupCreated.toDTO()
    }

    private fun findByUserOwnerAndSingle(userGroupDTO: UserGroupDTO): UsersGroup? {
        val userOwner = userGroupDTO.userOwner?.toDomain()

        if (userOwner?.id == null) return null

        return userGroupRepository.findByUserOwnerAndSingle(userOwner, single = true)
    }

    override fun findById(id: UUID): UserGroupDTO? = userGroupRepository.findById(id).orElse(null).let { it?.toDTO() }

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
}
