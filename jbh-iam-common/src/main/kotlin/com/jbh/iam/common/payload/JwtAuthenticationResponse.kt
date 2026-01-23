package com.jbh.iam.common.payload

data class JwtAuthenticationResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val error: String? = null
)