package com.jbh.iam.domain.modules.usergroup.model

import com.jbh.iam.api.dto.DTOConverter
import com.jbh.iam.api.util.DefaultValues
import com.jbh.iam.api.util.DefaultValues.MAX_DESC_NAMES_LENGTH
import com.jbh.iam.api.util.DefaultValues.MIN_DESC_NAMES_LENGTH
import com.jbh.iam.domain.modules.common.AuditModel
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*


@Entity
@Table(
    name = "user_group",
    schema = "iam"
)
open class UsersGroupEntity(

    @org.hibernate.annotations.GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    @Id
    val id: UUID? = null,

    @Column(name = "short_name")
    @get:NotBlank(message = "User Group short_name is required")
    val shortName: String = DefaultValues.EMPTY_STRING,


    @Column(name = "NAME")
    @get:NotBlank(message = "User Group nickName is required")
    @get:Size(
        min = MIN_DESC_NAMES_LENGTH,
        max = MAX_DESC_NAMES_LENGTH,
        message = "Name should have be between [$MIN_DESC_NAMES_LENGTH - $MAX_DESC_NAMES_LENGTH]"
    )
    val name: String = DefaultValues.EMPTY_STRING,


    ) : AuditModel(), DTOConverter<UserGroupDTO, UsersGroupEntity> {

    override fun toDTO(): UserGroupDTO {
        return UserGroupDTO(
            this.id,
            this.name,
        )
    }
}
