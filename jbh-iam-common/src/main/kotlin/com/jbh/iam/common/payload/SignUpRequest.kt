package com.jbh.iam.common.payload



data class SignUpRequest(
    var name: String = "",
    var username: String = "",
    //@Email
    var email: String = "",
    var password: String = ""
)