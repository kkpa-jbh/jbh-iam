package com.jbh.iam.common.payload



data class SignUpRequest(
    var name: String = "",
    var username: String = "",
    var email: String = "",
    var password: String = ""
)