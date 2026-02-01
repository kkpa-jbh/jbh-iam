package com.jbh.iam.cache.api

import com.jbh.iam.cache.api.dto.CachedUserDTO
import java.util.UUID

interface UserCacheOperations : CacheAdapter<UUID, CachedUserDTO> {
    fun getByEmail(email: String): CachedUserDTO?
    fun getByNickName(nickName: String): CachedUserDTO?
    fun getByNickNameOrEmail(nickName: String, email: String): CachedUserDTO?
    fun getAllByIds(ids: List<UUID>): List<CachedUserDTO>
    fun deleteUser(userId: UUID, email: String, nickName: String)
    fun evictUserId(userId: UUID)
}
