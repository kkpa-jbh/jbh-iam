package com.jbh.iam.api.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jbh.iam.api.util.DefaultValues
import com.jbh.iam.domain.usergroup.UsersGroup
import org.springframework.stereotype.Component
import java.util.*

@Component
data class UserGroupDTO(
    @JsonIgnore
    val id: UUID? = null,
    val name: String = DefaultValues.EMPTY_STRING,
    val shortName: String = DefaultValues.EMPTY_STRING,

    ) : JbhDTO, DomainConverter<UserGroupDTO, UsersGroup> {

    override fun toDomain(): UsersGroup {
        return UsersGroup(
            this.id,
            name = this.name,
            shortName = this.shortName
        )
    }
}
