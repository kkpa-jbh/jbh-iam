package com.jbh.iam.api.domain

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * DRY (Don't Repeat Yourself) principle: Defines common audit fields in one place, reducing code duplication
 */
@MappedSuperclass //To prevent null when saving a child.
@EntityListeners(AuditingEntityListener::class)
abstract class AuditModel {

    @Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt = LocalDateTime.now()

    @Column(name = "updated_at", nullable = true, updatable = true, insertable = false)
    open val updatedAt = LocalDateTime.now()
}
