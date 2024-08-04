package com.jbh.iam.api.services.accounts

import com.jbh.iam.api.dto.AccountsDTO
import java.util.*

interface AccountService {

    fun findAccountsByUserGroupId(userGroupId: UUID): List<AccountsDTO>
}