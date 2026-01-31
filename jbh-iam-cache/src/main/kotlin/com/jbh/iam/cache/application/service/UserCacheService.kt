package com.jbh.iam.cache.application.service

import com.jbh.iam.cache.api.UserCacheOperations
import com.jbh.iam.cache.api.dto.CachedUserDTO
import com.jbh.iam.cache.domain.port.outbound.CacheStoragePort
import java.util.UUID

class UserCacheService(
    private val userCacheStorage: CacheStoragePort<UUID, CachedUserDTO>,
    private val emailIndexStorage: CacheStoragePort<String, UUID>,
    private val nickNameIndexStorage: CacheStoragePort<String, UUID>
) : UserCacheOperations {

    override fun get(key: UUID): CachedUserDTO? = userCacheStorage.get(key)

    override fun getOrLoad(key: UUID, loader: () -> CachedUserDTO?): CachedUserDTO? {
        userCacheStorage.get(key)?.let { return it }
        return loader()?.also { put(key, it) }
    }

    override fun put(key: UUID, value: CachedUserDTO) {
        userCacheStorage.put(key, value)
        emailIndexStorage.put(value.email.lowercase(), key)
        nickNameIndexStorage.put(value.nickName.lowercase(), key)
    }

    override fun evict(key: UUID) {
        userCacheStorage.get(key)?.let { user ->
            emailIndexStorage.remove(user.email.lowercase())
            nickNameIndexStorage.remove(user.nickName.lowercase())
        }
        userCacheStorage.remove(key)
    }

    override fun evictAll() {
        userCacheStorage.clear()
        emailIndexStorage.clear()
        nickNameIndexStorage.clear()
    }

    override fun refresh(key: UUID, loader: () -> CachedUserDTO?) {
        evict(key)
        loader()?.let { put(key, it) }
    }

    override fun getByEmail(email: String): CachedUserDTO? {
        val userId = emailIndexStorage.get(email.lowercase()) ?: return null
        return userCacheStorage.get(userId)
    }

    override fun getByNickName(nickName: String): CachedUserDTO? {
        val userId = nickNameIndexStorage.get(nickName.lowercase()) ?: return null
        return userCacheStorage.get(userId)
    }

    override fun getByNickNameOrEmail(nickName: String, email: String): CachedUserDTO? {
        return getByNickName(nickName) ?: getByEmail(email)
    }

    override fun getAllByIds(ids: List<UUID>): List<CachedUserDTO> {
        return ids.mapNotNull { userCacheStorage.get(it) }
    }

    override fun evictUser(userId: UUID, email: String, nickName: String) {
        userCacheStorage.remove(userId)
        emailIndexStorage.remove(email.lowercase())
        nickNameIndexStorage.remove(nickName.lowercase())
    }
}
