package com.jbh.iam.cache.application.service

import com.jbh.iam.cache.api.UserCacheOperations
import com.jbh.iam.cache.api.dto.CachedUserDTO
import com.jbh.iam.cache.domain.port.outbound.CacheStoragePort
import org.slf4j.LoggerFactory
import java.util.UUID

class UserCacheService(
    private val userCacheStorage: CacheStoragePort<UUID, CachedUserDTO>,
    private val emailIndexStorage: CacheStoragePort<String, UUID>,
    private val nickNameIndexStorage: CacheStoragePort<String, UUID>
) : UserCacheOperations {

    private val log = LoggerFactory.getLogger(UserCacheService::class.java)

    override fun get(key: UUID): CachedUserDTO? {
        val result = userCacheStorage.get(key)
        if (result != null) {
            log.debug("CACHE HIT - get by id: {}", key)
        } else {
            log.debug("CACHE MISS - get by id: {}", key)
        }
        return result
    }

    override fun getOrLoad(key: UUID, loader: () -> CachedUserDTO?): CachedUserDTO? {
        userCacheStorage.get(key)?.let {
            log.debug("CACHE HIT - getOrLoad by id: {}", key)
            return it
        }
        log.debug("CACHE MISS - getOrLoad by id: {}, loading from source", key)
        return loader()?.also { put(key, it) }
    }

    override fun put(key: UUID, value: CachedUserDTO) {
        log.debug("CACHE PUT - user id: {}, email: {}, nickName: {}", key, value.email, value.nickName)
        userCacheStorage.put(key, value)
        emailIndexStorage.put(value.email.lowercase(), key)
        nickNameIndexStorage.put(value.nickName.lowercase(), key)
    }

    override fun evict(key: UUID) {
        userCacheStorage.get(key)?.let { user ->
            log.debug("CACHE EVICT - user id: {}, email: {}, nickName: {}", key, user.email, user.nickName)
            emailIndexStorage.remove(user.email.lowercase())
            nickNameIndexStorage.remove(user.nickName.lowercase())
        }
        userCacheStorage.remove(key)
    }

    override fun evictAll() {
        log.info("CACHE EVICT ALL - clearing all user cache entries")
        userCacheStorage.clear()
        emailIndexStorage.clear()
        nickNameIndexStorage.clear()
    }

    override fun refresh(key: UUID, loader: () -> CachedUserDTO?) {
        log.debug("CACHE REFRESH - user id: {}", key)
        evict(key)
        loader()?.let { put(key, it) }
    }

    override fun getByEmail(email: String): CachedUserDTO? {
        val userId = emailIndexStorage.get(email.lowercase())
        if (userId == null) {
            log.debug("CACHE MISS - getByEmail: {}", email)
            return null
        }
        val result = userCacheStorage.get(userId)
        if (result != null) {
            log.debug("CACHE HIT - getByEmail: {}, userId: {}", email, userId)
        } else {
            log.debug("CACHE MISS - getByEmail: {} (index found but user missing)", email)
        }
        return result
    }

    override fun getByNickName(nickName: String): CachedUserDTO? {
        val userId = nickNameIndexStorage.get(nickName.lowercase())
        if (userId == null) {
            log.debug("CACHE MISS - getByNickName: {}", nickName)
            return null
        }
        val result = userCacheStorage.get(userId)
        if (result != null) {
            log.debug("CACHE HIT - getByNickName: {}, userId: {}", nickName, userId)
        } else {
            log.debug("CACHE MISS - getByNickName: {} (index found but user missing)", nickName)
        }
        return result
    }

    override fun getByNickNameOrEmail(nickName: String, email: String): CachedUserDTO? {
        val result = getByNickName(nickName) ?: getByEmail(email)
        if (result != null) {
            log.debug("CACHE HIT - getByNickNameOrEmail: nickName={}, email={}", nickName, email)
        } else {
            log.debug("CACHE MISS - getByNickNameOrEmail: nickName={}, email={}", nickName, email)
        }
        return result
    }

    override fun getAllByIds(ids: List<UUID>): List<CachedUserDTO> {
        val results = ids.mapNotNull { userCacheStorage.get(it) }
        val hitCount = results.size
        val missCount = ids.size - hitCount
        log.debug("CACHE getAllByIds - requested: {}, hits: {}, misses: {}", ids.size, hitCount, missCount)
        return results
    }

    override fun deleteUser(userId: UUID, email: String, nickName: String) {
        log.debug("CACHE DELETE USER - userId: {}, email: {}, nickName: {}", userId, email, nickName)
        userCacheStorage.remove(userId)
        emailIndexStorage.remove(email.lowercase())
        nickNameIndexStorage.remove(nickName.lowercase())
    }

    override fun evictUserId(userId: UUID) {
        log.debug("CACHE EVICT USER ID - userId: {}", userId)
        userCacheStorage.remove(userId)
    }
}
