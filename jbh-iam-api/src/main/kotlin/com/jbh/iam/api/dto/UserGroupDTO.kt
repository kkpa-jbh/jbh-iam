package com.jbh.iam.api.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jbh.iam.api.domain.Users.UsersGroup
import com.jbh.iam.api.util.DefaultValues
import org.springframework.stereotype.Component
import java.util.*

@Component
data class UserGroupDTO(
    @JsonIgnore
    val id: UUID? = null,
    val name: String = DefaultValues.EMPTY_STRING,
    var single: Boolean = true,
    var userOwner: UserDTO? = null
) : JbhDTO, DomainConverter<UserGroupDTO, UsersGroup> {

    override fun toDomain(): UsersGroup {
        return UsersGroup(
            this.id,
            name = this.name,
            single = this.single,
            userOwner = this.userOwner!!.toDomain()
        )
    }
}
