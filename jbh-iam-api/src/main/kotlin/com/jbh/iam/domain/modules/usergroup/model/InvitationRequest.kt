package com.jbh.iam.domain.modules.usergroup.model

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class InvitationRequest(
    @field:NotBlank(message = "Email or username is required")
    val emailOrUsername: String,

    @field:NotBlank(message = "Team name is required")
    val teamName: String,

    val teamID: UUID,
)
