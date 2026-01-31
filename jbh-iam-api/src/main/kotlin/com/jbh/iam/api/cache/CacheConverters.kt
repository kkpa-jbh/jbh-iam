package com.jbh.iam.api.cache

import com.jbh.iam.cache.api.dto.CachedUserDTO
import com.jbh.iam.domain.modules.user.model.InternalUserDTO
import com.jbh.iam.domain.modules.user.model.PublicUserInfo
import java.time.Instant

fun InternalUserDTO.toCachedDTO(): CachedUserDTO {
    return CachedUserDTO(
        id = this.id,
        username = this.username,
        nickName = this.nickName,
        email = this.email,
        roles = this.roles,
        cachedAt = Instant.now()
    )
}

fun CachedUserDTO.toInternalDTO(): InternalUserDTO {
    return InternalUserDTO(
        id = this.id,
        publicInfo = PublicUserInfo(
            username = this.username,
            nickName = this.nickName,
            email = this.email,
            roles = this.roles
        ),
        password = "",
        roles = this.roles
    )
}
