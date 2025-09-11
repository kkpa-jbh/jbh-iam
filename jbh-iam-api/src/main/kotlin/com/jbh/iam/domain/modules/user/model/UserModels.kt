package com.jbh.iam.domain.modules.user.model

import com.jbh.iam.core.model.RoleName
import com.jbh.iam.core.model.UserCore
import com.jbh.iam.core.model.UserRole
import java.util.*

/**
 * Represents publicly shareable user information.
 * This class should be used for API responses and any context where
 * sensitive user data should not be exposed.
 */
data class PublicUserInfo(
    val username: String = "",
    val nickName: String = "",
    val email: String = ""
)

/**
 * Represents the complete user information for internal use.
 * This class includes sensitive information and should not be
 * exposed directly through public APIs.
 */
data class InternalUserDTO(
    val id: UUID = UUID.randomUUID(),
    val publicInfo: PublicUserInfo = PublicUserInfo(),
    val password: String = "",
    val roles: Set<RoleName> = setOf()
) {
    // Delegate properties to publicInfo
    val username: String by publicInfo::username
    val nickName: String by publicInfo::nickName
    val email: String by publicInfo::email

    /**
     * Secondary constructor for convenience when creating from individual fields.
     */
    constructor(
        id: UUID,
        username: String,
        nickName: String,
        email: String,
        password: String,
        roles: Set<RoleName>
    ) : this(id, PublicUserInfo(username, nickName, email), password, roles)
}

// You can add extension functions or additional related utilities here
fun InternalUserDTO.toPublicInfo() = this.publicInfo

fun InternalUserDTO.toDomain(): UserEntity {
    return UserEntity(
        this.id!!,
        this.publicInfo.username,
        this.nickName,
        this.email,
        this.password,
        this.roles.map { RoleEntity(null, it) }.toSet()
    )
}

fun InternalUserDTO.toUserCore(): UserCore {
    return UserCore(
        this.id!!,
        this.publicInfo.username,
        this.nickName,
        this.email,
        this.password,
        this.roles.map { UserRole(null, it) }.toSet()
    )
}

fun UserEntity.toPublicInfo(): PublicUserInfo {
    return PublicUserInfo(this.userName, this.nickName, this.email)
}

fun UserEntity.toInternalUserDTO(): InternalUserDTO {
    return InternalUserDTO(this.id!!, this.toPublicInfo(), this.password, this.roles.map { it.name }.toSet())
}