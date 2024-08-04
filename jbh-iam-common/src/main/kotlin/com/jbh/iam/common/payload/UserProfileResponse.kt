package com.jbh.iam.common.payload

import java.time.Instant
import java.util.UUID

data class UserProfileResponse(
    var id: UUID,
    var username: String,
    var name: String,
    var joinedAt: Instant?,
    var pollCount: Long = 0,
    var voteCount: Long = 0
)
