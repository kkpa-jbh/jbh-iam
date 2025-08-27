package com.jbh.iam.domain.modules.usergroup.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jbh.iam.api.dto.DomainConverter
import com.jbh.iam.api.dto.JbhDTO
import com.jbh.iam.api.util.DefaultValues
import org.springframework.stereotype.Component
import java.util.*

@Component
data class UserGroupDTO(
    @JsonIgnore
    val id: UUID? = null,
    val name: String = DefaultValues.EMPTY_STRING,
    val shortName: String = DefaultValues.EMPTY_STRING,

    ) : JbhDTO, DomainConverter<UserGroupDTO, UsersGroupEntity> {

    override fun toDomain(): UsersGroupEntity {
        return UsersGroupEntity(
            this.id,
            name = this.name,
            shortName = this.shortName
        )
    }
}
