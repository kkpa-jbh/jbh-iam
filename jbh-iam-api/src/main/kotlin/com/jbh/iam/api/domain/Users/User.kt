package com.jbh.iam.api.domain.Users

import com.jbh.iam.api.domain.AuditModel
import com.jbh.iam.api.domain.DTOConverter
import com.jbh.iam.api.dto.UserDTO
import com.jbh.iam.api.util.DefaultValues
import com.jbh.iam.core.model.RoleName
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.*

@Entity
/**
 * This constraints are taken and created into the database. (Anonymous way)
 */
@Table(
    name = "JBH_USER",
    uniqueConstraints = [(UniqueConstraint(columnNames = ["nick_name", "email"]))]
)
open class User(
    @org.hibernate.annotations.GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    @Id
    val id: UUID? = null,

    @Column(name = "user_name")
    @get:Size(
        min = DefaultValues.MIN_DESC_NAMES_LENGTH,
        message = "Name should have at least ${DefaultValues.MIN_DESC_NAMES_LENGTH} and maximum ${DefaultValues.MAX_DESC_NAMES_LENGTH} characters",
        max = DefaultValues.MAX_DESC_NAMES_LENGTH
    )
    val userName: String = DefaultValues.EMPTY_STRING,

    @Column(name = "email", unique = true, nullable = false, updatable = true)
    @get:NotEmpty(message = "Email can not be empty")
    @field:Email(message = "Email should be valid")
    val email: String = DefaultValues.EMPTY_STRING,

    @Column(name = "nick_name")
    @get:NotEmpty(message = "User nickName can not be empty")
    @get:Size(
        min = 3,
        message = "Name should have at least ${DefaultValues.MIN_DESC_NAMES_LENGTH} and maximum ${DefaultValues.MAX_DESC_NAMES_LENGTH} characters",
        max = DefaultValues.MAX_DESC_NAMES_LENGTH
    )
    val nickName: String = DefaultValues.EMPTY_STRING,

    @Column(name = "password", nullable = false)
    @get:NotNull(message = "Password can not be null")
    @get:NotEmpty(message = "Password can not be empty")
    var password: String = DefaultValues.EMPTY_STRING,

    @ManyToMany(fetch = FetchType.LAZY, cascade = arrayOf(CascadeType.ALL))
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: Set<Role> = mutableSetOf(Role())

) : AuditModel(), DTOConverter<UserDTO, User> {

    override fun toDTO(): UserDTO {
        return UserDTO(
            id = this.id,
            userName = this.userName,
            password = this.password,
            email = this.email,
            nickName = this.nickName,
            roles = this.roles,
            createdAt = this.createdAt

        )
    }

    fun isAdmin(): Boolean = this.roles.map { it.name }.contains(RoleName.ROLE_ADMIN)
}
