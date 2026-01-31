package com.jbh.iam.cache.api.dto

import com.jbh.iam.core.model.RoleName
import java.time.Instant
import java.util.UUID

data class CachedUserDTO(
    val id: UUID,
    val username: String,
    val nickName: String,
    val email: String,
    val roles: Set<RoleName>,
    val cachedAt: Instant = Instant.now()
)
