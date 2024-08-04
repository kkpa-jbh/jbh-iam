package com.jbh.iam.api.domain.Users

import com.jbh.iam.core.model.RoleName
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "roles")
data class Role(
    @org.hibernate.annotations.GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    @Id
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 60)
    var name: RoleName = RoleName.ROLE_USER
)