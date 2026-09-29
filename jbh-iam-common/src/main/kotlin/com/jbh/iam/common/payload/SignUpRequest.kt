package com.jbh.iam.common.payload



data class SignUpRequest(
    var name: String = "",
    var username: String = "",
    var email: String = "",
    var password: String = ""
) {
    // Never print the password: this object is written to the log.
    override fun toString(): String =
        "SignUpRequest(name=$name, username=$username, email=$email, password=***)"
}
