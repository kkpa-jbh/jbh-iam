package com.jbh.iam.api.services.users

import com.jbh.iam.api.cache.toCachedDTO
import com.jbh.iam.api.cache.toInternalDTO
import com.jbh.iam.api.exceptions.web.ResourceNotFoundException
import com.jbh.iam.cache.api.UserCacheOperations
import com.jbh.iam.domain.modules.user.model.*
import com.jbh.iam.domain.modules.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userCache: UserCacheOperations
) : UserService {

    private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)

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
        log.info("Persisting user: {}", userDomain.nickName)
        validateUpdateOperation(userDomain.toInternalUserDTO())
        val saved = userRepository.save(userDomain).toInternalUserDTO()
        userCache.put(saved.id, saved.toCachedDTO())
        log.debug("User saved and cached - id: {}, nickName: {}", saved.id, saved.nickName)
        return saved
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
        val saved = userRepository.save(dto.toDomain()).toInternalUserDTO()
        userCache.put(saved.id, saved.toCachedDTO())
        log.debug("User updated and cached - id: {}, nickName: {}", saved.id, saved.nickName)
        return saved
    }

    fun delete(dto: InternalUserDTO): Boolean {
        dto?.also {
            try {
                userRepository.deleteById(dto.id!!)
                userCache.evictUser(dto.id, dto.email, dto.nickName)
                log.info("User deleted and evicted from cache - id: {}, nickName: {}", dto.id, dto.nickName)
                return true
            } catch (enf: Exception) {
                throw java.lang.IllegalArgumentException("User  $it not found")
            }
        }

        throw java.lang.IllegalArgumentException("Not user to be deleted")
    }

    fun findAll(): List<PublicUserInfo> {
        log.debug("Finding all users from database (no cache)")
        val users = userRepository.findAll()
        return users.parallelStream().map { it.toPublicInfo() }.toList()
    }

    override fun findById(id: UUID): InternalUserDTO {
        log.debug("findById - looking for user id: {}", id)
        return userCache.getOrLoad(id) {
            log.debug("findById - CACHE MISS, loading from database: {}", id)
            userRepository.findById(id).orElse(null)?.toInternalUserDTO()?.toCachedDTO()
        }?.toInternalDTO()
            ?: throw IllegalArgumentException("User not found with id $id")
    }

    override fun findByEmail(email: String): InternalUserDTO {
        log.debug("findByEmail - looking for: {}", email)
        userCache.getByEmail(email)?.let {
            log.debug("findByEmail - CACHE HIT for email: {}", email)
            return it.toInternalDTO()
        }

        log.debug("findByEmail - CACHE MISS, loading from database: {}", email)
        val user = userRepository.findByEmail(email)
            ?: throw java.lang.IllegalArgumentException(" User not found with email $email")

        val internalDto = user.toInternalUserDTO()
        userCache.put(internalDto.id, internalDto.toCachedDTO())
        return internalDto
    }

    override fun findAllByIds(ids: List<UUID>): List<InternalUserDTO> {
        log.debug("findAllByIds - looking for {} users", ids.size)
        val cachedUsers = userCache.getAllByIds(ids)
        val cachedIds = cachedUsers.map { it.id }.toSet()
        val missingIds = ids.filter { it !in cachedIds }

        log.debug("findAllByIds - CACHE HIT: {}, CACHE MISS: {}", cachedUsers.size, missingIds.size)

        val fromDb = if (missingIds.isNotEmpty()) {
            log.debug("findAllByIds - loading {} users from database", missingIds.size)
            userRepository.findAllById(missingIds).map { entity ->
                val dto = entity.toInternalUserDTO()
                userCache.put(dto.id, dto.toCachedDTO())
                dto
            }
        } else emptyList()

        return cachedUsers.map { it.toInternalDTO() } + fromDb
    }

    override fun existsByNickName(nickName: String): Boolean {
        return userRepository.existsByNickName(nickName)
    }

    override fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    override fun findByNickNameOrEmail(nickName: String, email: String): InternalUserDTO {
        log.debug("findByNickNameOrEmail - looking for nickName: {}, email: {}", nickName, email)
        userCache.getByNickNameOrEmail(nickName, email)?.let {
            log.debug("findByNickNameOrEmail - CACHE HIT for nickName: {} or email: {}", nickName, email)
            return it.toInternalDTO()
        }

        log.debug("findByNickNameOrEmail - CACHE MISS, loading from database")
        val user = userRepository.findByNickNameOrEmail(nickName, email)
            ?: throw IllegalArgumentException("User not found with username or email $email")

        val internalDto = user.toInternalUserDTO()
        userCache.put(internalDto.id, internalDto.toCachedDTO())
        return internalDto
    }

    fun findByNickName(nickName: String): PublicUserInfo {
        log.debug("findByNickName - looking for: {}", nickName)
        userCache.getByNickName(nickName)?.let {
            log.debug("findByNickName - CACHE HIT for nickName: {}", nickName)
            return it.toInternalDTO().toPublicInfo()
        }

        log.debug("findByNickName - CACHE MISS, loading from database: {}", nickName)
        userRepository.findByNickName(nickName)?.let {
            val dto = it.toInternalUserDTO()
            userCache.put(dto.id, dto.toCachedDTO())
            return it.toPublicInfo()
        }
        throw ResourceNotFoundException("User", "nickname", nickName)
    }
}
