package com.jbh.iam.api.repositories.accounts

import com.jbh.iam.api.domain.accounts.Accounts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountRepository : JpaRepository<Accounts, UUID> {

    fun findByUserGroupId(userGroupId: UUID): List<Accounts>
}