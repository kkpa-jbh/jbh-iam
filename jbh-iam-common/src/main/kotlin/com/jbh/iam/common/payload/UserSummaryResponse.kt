package com.jbh.iam.common.payload

import java.util.UUID

data class UserSummaryResponse(
    val id: UUID,
    val username: String,
    val name: String
)