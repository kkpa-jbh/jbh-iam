package com.jbh.iam.domain.modules.usergroup.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateUserGroupRequest(
    @field:NotBlank(message = "Short name is required")
    @field:Size(min = 2, max = 20, message = "Short name must be between 2 and 20 characters")
    val shortName: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val name: String
)

data class UpdateUserGroupRequest(
    @field:Size(min = 2, max = 20, message = "Short name must be between 2 and 20 characters")
    val shortName: String? = null,

    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val name: String? = null
)
