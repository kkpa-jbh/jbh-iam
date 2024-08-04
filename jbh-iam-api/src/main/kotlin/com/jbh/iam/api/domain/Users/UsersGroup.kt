package com.jbh.iam.api.domain.Users

import com.jbh.iam.api.domain.AuditModel
import com.jbh.iam.api.domain.DTOConverter
import com.jbh.iam.api.dto.UserGroupDTO
import com.jbh.iam.api.util.DefaultValues
import com.jbh.iam.api.util.DefaultValues.MAX_DESC_NAMES_LENGTH
import com.jbh.iam.api.util.DefaultValues.MIN_DESC_NAMES_LENGTH
import jakarta.persistence.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*


@Entity
open class UsersGroup(

    @org.hibernate.annotations.GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    @Id
    val id: UUID? = null,

    @Column(name = "NAME")
    @get:NotBlank(message = "User Group nickName is required")
    @get:Size(
        min = MIN_DESC_NAMES_LENGTH,
        max = MAX_DESC_NAMES_LENGTH,
        message = "Name should have be between [$MIN_DESC_NAMES_LENGTH - $MAX_DESC_NAMES_LENGTH]"
    )
    val name: String = DefaultValues.EMPTY_STRING,

    /**
     * Identify it the group is just for the user.
     */
    @Column(name = "single")
    val single: Boolean = true,

    @OneToOne(cascade = [(CascadeType.REFRESH), (CascadeType.DETACH), (CascadeType.MERGE)])
    @JoinColumn(name = "user_id")
    @field:Valid // To Valid entity user against constraint violations exceptions when it's saving an userGroupd
    val userOwner: User = User()

) : AuditModel(), DTOConverter<UserGroupDTO, UsersGroup> {

    override fun toDTO(): UserGroupDTO {
        return UserGroupDTO(
            this.id,
            this.name,
            this.single,
            this.userOwner.toDTO()
        )
    }
}
