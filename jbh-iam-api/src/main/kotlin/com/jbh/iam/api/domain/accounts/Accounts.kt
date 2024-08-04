package com.jbh.iam.api.domain.accounts

import com.jbh.iam.api.domain.AuditModel
import com.jbh.iam.api.domain.DTOConverter
import com.jbh.iam.api.domain.Users.UsersGroup
import com.jbh.iam.api.dto.AccountsDTO
import com.jbh.iam.api.util.DefaultValues.EMPTY_STRING
import com.jbh.iam.api.util.DefaultValues.MAX_DESC_NAMES_LENGTH
import com.jbh.iam.api.util.DefaultValues.MIN_DESC_NAMES_LENGTH
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.*

@Entity
data class Accounts(

    @org.hibernate.annotations.GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    @Id
    val id: UUID? = null,

    @Column(name = "description")
    @field:NotEmpty(message = "Description is required") // See https://stackoverflow.com/questions/35847763/kotlin-data-class-bean-validation-jsr-303
    @field:Size(
        min = MIN_DESC_NAMES_LENGTH,
        message = "Description should have at least $MIN_DESC_NAMES_LENGTH characters",
        max = MAX_DESC_NAMES_LENGTH
    )
    val description: String = EMPTY_STRING,

    @Column(name = "active_balance")
    val activeBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "passive_balance")
    val passiveBalance: BigDecimal = BigDecimal.ZERO,

    @OneToOne
    @JoinColumn(name = "user_group_id")
    val userGroup: UsersGroup = UsersGroup(),


    ) : AuditModel(), DTOConverter<AccountsDTO, Accounts> {

    override fun toDTO(): AccountsDTO {
        return AccountsDTO(
            this.id,
            this.description,
            this.activeBalance,
            this.passiveBalance,
            this.userGroup.toDTO()
        )
    }


}