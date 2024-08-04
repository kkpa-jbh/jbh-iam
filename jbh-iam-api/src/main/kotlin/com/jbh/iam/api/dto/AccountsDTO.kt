package com.jbh.iam.api.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.jbh.iam.api.domain.accounts.Accounts
import com.jbh.iam.api.util.DefaultValues
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
data class AccountsDTO(

    val id: UUID? = null,
    val description: String = DefaultValues.EMPTY_STRING,
    val activeBalance: BigDecimal = BigDecimal.ZERO,
    val passiveBalance: BigDecimal = BigDecimal.ZERO,
    @JsonIgnoreProperties
    @JsonIgnore
    val userGroup: UserGroupDTO = UserGroupDTO()

) : JbhDTO, DomainConverter<AccountsDTO, Accounts> {

    override fun toDomain(): Accounts {
        val accountDB = Accounts(
            this.id,
            this.description,
            this.activeBalance,
            this.passiveBalance,
            this.userGroup.toDomain()
        )

        return accountDB
    }
}