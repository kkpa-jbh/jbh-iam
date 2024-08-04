package com.jbh.iam.api.services.accounts

import com.jbh.iam.api.domain.toDTOList
import com.jbh.iam.api.dto.AccountsDTO
import com.jbh.iam.api.repositories.accounts.AccountRepository
import com.jbh.iam.api.services.CrudService
import com.jbh.iam.api.services.users.UserGroupServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.stereotype.Service
import java.util.*

@Service
class AccountServiceImpl(
    @Autowired private val accountRepository: AccountRepository,
    @Autowired private val userGroupServiceImpl: UserGroupServiceImpl
) : CrudService<AccountsDTO>(), AccountService {

    override fun validateSaveOperation(dto: AccountsDTO) {
        if (isNewAndUnique(dto).not()) throw IllegalArgumentException("Account needs to be updated.")

        if (accountHasntGroupAssociated(dto)) throw IllegalArgumentException("Account don't belongs to any group")
    }

    fun accountHasntGroupAssociated(accountDTO: AccountsDTO): Boolean {
        if (accountDTO.userGroup.id != null) {
            return userGroupServiceImpl.findById(accountDTO.userGroup.id!!) == null
        }

        return true
    }

    override fun isNewAndUnique(dto: AccountsDTO): Boolean = dto.id == null || findById(dto.id!!) == null

    override fun saveOperation(dto: AccountsDTO): AccountsDTO {
        return accountRepository.save(dto.toDomain()).toDTO()
    }

    override fun validateUpdateOperation(dto: AccountsDTO) {
    }

    override fun updateOperation(dto: AccountsDTO): AccountsDTO {
        return accountRepository.save(dto.toDomain()).toDTO()
    }

    override fun delete(dto: AccountsDTO): Boolean {

        if (dto.id == null) {
            throw IllegalArgumentException("Not accountId to be deleted")
        }

        try {
            accountRepository.delete(dto.toDomain())
        } catch (enf: JpaObjectRetrievalFailureException) {
            throw IllegalArgumentException("Account ${dto.id} not found")
        }

        return true
    }

    override fun findAll(): List<AccountsDTO> {

        return accountRepository.findAll().toDTOList()
    }

    override fun findById(id: UUID): AccountsDTO? {
        return accountRepository.findById(id).orElse(null)?.let {
            it.toDTO()
        }
    }

    override fun findAccountsByUserGroupId(userGroupId: UUID): List<AccountsDTO> {
        return accountRepository.findByUserGroupId(userGroupId).toDTOList()
    }
}