package com.jbh.iam.api.services.users

import com.jbh.iam.api.exceptions.web.ResourceNotFoundException
import com.jbh.iam.domain.modules.user.model.*
import com.jbh.iam.domain.modules.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    val log = LoggerFactory.getLogger(UserServiceImpl::class.java)

    fun validateSaveOperation(dto: InternalUserDTO) {
        if (!isNewAndUnique(dto)) {
            throw IllegalArgumentException("User ${dto.email} - ${dto.nickName} already was registered.")
        }
    }

    fun isNewAndUnique(dto: InternalUserDTO): Boolean {
        return dto.id == null && findByUniqueUser(dto) == null
    }

    @Transactional(readOnly = true)
    override fun findByUniqueUser(userDTO: InternalUserDTO): InternalUserDTO {
        try {
            return userRepository.findByNickNameOrEmail(
                nickName = userDTO.nickName,
                email = userDTO.email
            )?.toInternalUserDTO() ?: throw IllegalArgumentException("User not found with username or email $userDTO")
        } catch (e: org.springframework.dao.IncorrectResultSizeDataAccessException) {
            throw IllegalArgumentException("Nickname and email already were registered!")
        }
    }

    override fun save(userDomain: UserEntity): InternalUserDTO {
        log.info("Persisting user...${userDomain.nickName}")
        validateUpdateOperation(userDomain.toInternalUserDTO())
        return userRepository.save(userDomain).toInternalUserDTO()
    }

    fun validateUpdateOperation(dto: InternalUserDTO) {
        userAlreadyExistsThenThrowAnException(dto)
    }

    fun userAlreadyExistsThenThrowAnException(dto: InternalUserDTO) {
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

    fun updateOperation(dto: InternalUserDTO): InternalUserDTO {
        return userRepository.save(dto.toDomain()).toInternalUserDTO()
    }

    fun delete(dto: InternalUserDTO): Boolean {
        dto?.also {
            try {
                userRepository.deleteById(dto.id!!); return true
            } catch (enf: Exception) {
                throw java.lang.IllegalArgumentException("User  $it not found")
            }
        }

        throw java.lang.IllegalArgumentException("Not user to be deleted")
    }

    fun findAll(): List<PublicUserInfo> {
        val users = userRepository.findAll()

        return users.parallelStream().map { it.toPublicInfo() }.toList()
    }

    override fun findById(id: UUID): InternalUserDTO {
        return userRepository.findById(id).run {
            return this.isPresent.let {
                if (it) this.get().toInternalUserDTO() else throw IllegalArgumentException("User not found with id $id")
            }
        }
    }

    override fun findByEmail(email: String): InternalUserDTO {
       val user =  userRepository.findByEmail(email)
           ?: throw java.lang.IllegalArgumentException(" User not found with email $email")

        return user.toInternalUserDTO()
    }

    override fun findAllByIds(ids: List<UUID>): List<InternalUserDTO> {
        return userRepository.findAllById(ids).map { it.toInternalUserDTO() }
    }

    override fun existsByNickName(nickName: String): Boolean {
        return userRepository.existsByNickName(nickName)
    }

    override fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    override fun findByNickNameOrEmail(nickName: String, email: String): InternalUserDTO {
        val user = userRepository.findByNickNameOrEmail(nickName, email)
            ?: throw IllegalArgumentException("User not found with username or email $email")

        return user.toInternalUserDTO()
    }

    fun findByNickName(nickName: String): PublicUserInfo {
        userRepository.findByNickName(nickName)?.let {
            return it.toPublicInfo()
        }
        throw ResourceNotFoundException("User", "nickname", nickName)
    }
}
