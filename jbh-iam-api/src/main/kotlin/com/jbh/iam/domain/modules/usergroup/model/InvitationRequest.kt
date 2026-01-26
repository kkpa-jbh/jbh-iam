package com.jbh.iam.domain.modules.usergroup.model

import jakarta.validation.constraints.NotBlank

data class InvitationRequest(
    @field:NotBlank(message = "Email or username is required")
    val emailOrUsername: String
)
