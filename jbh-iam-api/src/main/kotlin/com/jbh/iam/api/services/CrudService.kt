package com.jbh.iam.api.services

import com.jbh.iam.api.dto.DomainConverter
import com.jbh.iam.api.exceptions.ConstraintViolationsException
import com.jbh.iam.common.payload.ErrorMessageResponse
import com.jbh.iam.common.payload.ErrorsResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import java.util.*

@Component
abstract class CrudService<T> {

    protected val log = LoggerFactory.getLogger(CrudService::class.java)

    @Autowired
    lateinit var validator: Validator

    abstract fun findAll(): List<T>

    abstract fun findById(id: UUID): T?

    abstract fun isNewAndUnique(dto: T): Boolean

    fun save(dto: T): T {
        validateEntity(dto)
        validateSaveOperation(dto)
        try {
            return saveOperation(dto)
        } catch (e: Throwable) {
            registerException(e)
            throw e
        }
    }

    /**
     * Business validation
     */
    @Throws(IllegalArgumentException::class)
    protected abstract fun validateSaveOperation(dto: T)

    fun update(dto: T): T {
        validateEntity(dto)
        validateUpdateOperation(dto)
        try {
            return updateOperation(dto)
        } catch (e: Throwable) {
            registerException(e)
            throw e
        }
    }

    protected abstract fun saveOperation(dto: T): T

    /**
     * Business validation
     */
    @Throws(IllegalArgumentException::class)
    protected abstract fun validateUpdateOperation(dto: T)

    protected abstract fun updateOperation(dto: T): T

    abstract fun delete(dto: T): Boolean

    // TODO Register into Kibana or error message table.
    private fun registerException(e: Throwable) {
        log.error("[Registering Exception ${e.printStackTrace()} - ${e.message}")
    }

    /**
     * As most of the save operation are use the save method of JpaRepository, it will not trigger the
     * constraint violation when it save an entity. (Instead of saveAndFlush, it commit and trigger the constraint violation automatically).
     *
     * So, that's why we're validating programmatically before to apply the save operation from hibernate.
     */
    @Throws(ConstraintViolationsException::class)
    private fun validateEntity(dto: T) {
        val dtoToValidate = (dto as DomainConverter<*, *>)
        val constraintViolations = dtoToValidate.toDomain()?.let { validator.validateObject(it) }
        if (constraintViolations != null) {
            if (constraintViolations.hasErrors()) {
                val errors = mutableSetOf<ErrorMessageResponse>()
                constraintViolations.allErrors.forEach {
                    with(it.objectName) {
                        log.error(this)
                        errors.add(ErrorMessageResponse(this))
                    }
                }
                throw ConstraintViolationsException(ErrorsResponse(errors))
            }
        }
    }
}
