package com.jbh.iam.api.services.users

import com.jbh.iam.api.domain.Users.User
import com.jbh.iam.api.domain.toDTOList
import com.jbh.iam.api.dto.UserDTO
import com.jbh.iam.api.exceptions.web.ResourceNotFoundException
import com.jbh.iam.api.repositories.users.UserGroupRepository
import com.jbh.iam.api.repositories.users.UserRepository
import com.jbh.iam.api.services.CrudService
import com.jbh.iam.core.model.UserCore
import com.jbh.iam.core.model.UserGroupCore
import com.jbh.iam.core.model.UserRole
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userGroupRepository: UserGroupRepository
) : CrudService<UserDTO>(), UserService {


    companion object {
        val log = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    override fun validateSaveOperation(dto: UserDTO) {
        if (!isNewAndUnique(dto)) {
            throw IllegalArgumentException("User ${dto.email} - ${dto.nickName} already was registered.")
        }
    }

    override fun isNewAndUnique(dto: UserDTO): Boolean {
        return dto.id == null && findByUniqueUser(dto) == null
    }

    @Transactional(readOnly = true)
    override fun findByUniqueUser(userDTO: UserDTO): UserDTO? {
        try {
            return userRepository.findByNickNameOrEmail(
                nickName = userDTO.nickName,
                email = userDTO.email
            )?.toDTO() ?: null
        } catch (e: org.springframework.dao.IncorrectResultSizeDataAccessException) {
            throw IllegalArgumentException("Nickname and email already were registered!")
        }
    }

    override fun saveOperation(userDTO: UserDTO): UserDTO {
        log.warn("Verifying Password...")
        var userToSave = userDTO.toDomain();
        return userRepository.save(userToSave).toDTO()
    }

    override fun validateUpdateOperation(dto: UserDTO) {
        userAlreadyExistsThenThrowAnException(dto)
    }

    fun userAlreadyExistsThenThrowAnException(dto: UserDTO) {
        val userBD = findByUniqueUser(dto)
        userBD?.let {

            dto.id?.let {
                if (userBD.id == dto.id) return
            }

            val propertyExisting = if (dto.email == it.email) "${dto.email}"
            else
                "${dto.nickName}"
            throw IllegalArgumentException("User [$propertyExisting] was already Registered")
        }
    }

    override fun updateOperation(dto: UserDTO): UserDTO {
        return userRepository.save(dto.toDomain()).toDTO()
    }

    override fun delete(dto: UserDTO): Boolean {
        dto?.also {
            try {
                userRepository.deleteById(dto.id!!); return true
            } catch (enf: Exception) {
                throw java.lang.IllegalArgumentException("User  $it not found")
            }
        }

        throw java.lang.IllegalArgumentException("Not user to be deleted")
    }

    override fun findAll(): List<UserDTO> {
        val users = userRepository.findAll()

        return users.toDTOList()
    }

    override fun findById(id: UUID): UserDTO? {
        val user = userRepository.findById(id).orElse(null)

        user?.let { it.toDTO() }

        return null
    }

    override fun existsByNickName(nickName: String): Boolean {
        return userRepository.existsByNickName(nickName)
    }

    override fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    override fun findByNickNameOrEmail(nickName: String, email: String): UserCore? {
        val user = userRepository.findByNickNameOrEmail(nickName, email)
            ?: throw IllegalArgumentException("User not found with username or email $email")
        log.info("loading user by userName... User Found! ${user.nickName}")

        return mapUserToCore(user)
    }

    private fun mapUserToCore(user: User): UserCore {
        val userRoles = user.roles.map { UserRole(it.id, it.name) }.toSet()

        val userCore = UserCore(
            id = user.id,
            userName = user.userName,
            email = user.email,
            nickName = user.nickName,
            password = user.password,
            roles = userRoles
        )
        return userCore
    }


    override fun findByUserOwnerId(id: UUID): UserGroupCore? {
        val userGroup = userGroupRepository.findByUserOwnerId(id)
            ?: throw IllegalArgumentException("User not found with id : $id")

        val userGroupCore = UserGroupCore(
            id = userGroup.id,
            name = userGroup.name,
            single = userGroup.single,
            userOwner = mapUserToCore(userGroup.userOwner),
        )
        log.info("loading user by ownerId... User Found! ${userGroup.userOwner?.nickName}")

        return userGroupCore
    }

    fun findByNickName(nickName: String): UserDTO {
        userRepository.findByNickName(nickName)?.let {
            return it.toDTO()
        }
        throw ResourceNotFoundException("User", "nickname", nickName)
    }
}
