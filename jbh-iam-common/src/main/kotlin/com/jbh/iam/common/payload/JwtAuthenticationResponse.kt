package com.jbh.iam.common.payload

class JwtAuthenticationResponse(var accessToken: String?) {
    var tokenType = "Bearer"
}