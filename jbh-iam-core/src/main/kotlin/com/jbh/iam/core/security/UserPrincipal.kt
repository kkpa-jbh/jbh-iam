package com.jbh.iam.core.security

import com.jbh.iam.core.model.UserCore
import com.jbh.iam.core.model.UserGroupCore
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class UserPrincipal(
    val id: UUID?,
    val userGroup: UserGroupCore?,
    val name: String,
    private val userName: String,
    val email: String,
    val jbhPassword: String,
    val jbhAuthorities: List<SimpleGrantedAuthority>
) : UserDetails {

    companion object {
        fun create(user: UserCore): UserPrincipal {
            val authorities = user.roles.stream().map { role -> SimpleGrantedAuthority(role.name.toString()) }.toList()

            return UserPrincipal(
                id = user.id,
                userGroup = null,
                name = user.userName,
                userName = user.nickName,
                email = user.email,
                jbhPassword = user.password,
                jbhAuthorities = authorities
            )
        }

        fun create(userGroup: UserGroupCore): UserPrincipal {
            val user = userGroup.userOwner
            val authorities = user.roles.stream().map { role -> SimpleGrantedAuthority(role.name.toString()) }.toList()

            return UserPrincipal(
                id = user.id,
                userGroup = userGroup,
                name = user.userName,
                userName = user.nickName,
                email = user.email,
                jbhPassword = user.password,
                jbhAuthorities = authorities
            )
        }
    }

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun getUsername(): String = userName

    override fun getPassword(): String = jbhPassword

    override fun getAuthorities(): List<SimpleGrantedAuthority> = jbhAuthorities

    override fun isEnabled(): Boolean = true
}