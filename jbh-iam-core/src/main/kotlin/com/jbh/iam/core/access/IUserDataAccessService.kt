package com.jbh.iam.core.access

import com.jbh.iam.core.model.UserCore
import org.springframework.stereotype.Component
import java.util.*

@Component
interface IUserDataAccessService {
    fun findByNickNameOrEmail(nickName: String, email: String): UserCore?

    fun findByUserId(id: UUID): UserCore?
}