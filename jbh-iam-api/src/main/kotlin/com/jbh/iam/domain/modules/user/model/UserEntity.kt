package com.jbh.iam.domain.modules.user.model

import com.jbh.iam.api.util.DefaultValues
import com.jbh.iam.domain.modules.common.AuditModel
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
    name = "user",
    schema = "iam",
    uniqueConstraints = [(UniqueConstraint(columnNames = ["nick_name", "email"]))]
)
open class UserEntity(
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
        schema = "iam",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: Set<RoleEntity> = mutableSetOf(RoleEntity()),

    // Last on purpose: toDomain() builds UserEntity with positional arguments.
    // Set on the first Google sign-in. It is var so an existing password user can be linked later.
    @Column(name = "google_sub", unique = true)
    var googleSub: String? = null

) : AuditModel() {

}
