package com.jbh.iam.domain.modules.usergroup.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.*

@Embeddable
data class UserGroupMembershipId(
    @Column(name = "group_id")
    val groupId: UUID = UUID.randomUUID(),

    @Column(name = "user_id")
    val userId: UUID = UUID.randomUUID()
) : Serializable
