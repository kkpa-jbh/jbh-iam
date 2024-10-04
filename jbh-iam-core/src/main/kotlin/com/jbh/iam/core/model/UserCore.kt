package com.jbh.iam.core.model

import com.jbh.iam.common.config.DefaultValues

import java.util.*

data class UserCore(
    val id: UUID? = null,
    val userName: String = DefaultValues.EMPTY_STRING,
    val email: String = DefaultValues.EMPTY_STRING,
    val nickName: String = DefaultValues.EMPTY_STRING,
    var password: String = DefaultValues.EMPTY_STRING,
    var roles: Set<UserRole> = mutableSetOf(UserRole())
)