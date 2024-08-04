package com.jbh.iam.api.dto

import com.jbh.iam.api.domain.Users.Role
import com.jbh.iam.api.domain.Users.User
import com.jbh.iam.api.util.DefaultValues.EMPTY_STRING
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
data class UserDTO(
    val id: UUID? = null,
    val userName: String = EMPTY_STRING,
    val email: String = EMPTY_STRING,
    var password: String = EMPTY_STRING,
    val nickName: String = EMPTY_STRING,
    val roles: Set<Role> = emptySet(),
    val createdAt: LocalDateTime = LocalDateTime.now()
) : DomainConverter<UserDTO, User> {

    override fun toDomain(): User {
        var user = User(
            id = this.id,
            userName = this.userName,
            email = this.email,
            password = this.password,
            nickName = this.nickName,
            roles = this.roles
        )
        user.createdAt = this.createdAt;
        return user;
    }
}
