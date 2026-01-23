package com.jbh.iam.domain.modules.user.model

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "refresh_token", schema = "iam")
class RefreshTokenEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @org.hibernate.annotations.GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "token_hash", nullable = false, unique = true)
    val tokenHash: String,

    @Column(name = "issued_at", nullable = false)
    val issuedAt: Instant = Instant.now(),

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @Column(name = "revoked", nullable = false)
    var revoked: Boolean = false,

    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,

    @Column(name = "device_info")
    val deviceInfo: String? = null,

    @Column(name = "ip_address", length = 45)
    val ipAddress: String? = null
) {
    constructor() : this(null, UserEntity(), "", Instant.now(), Instant.now(), false, null, null, null)

    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    fun revoke() {
        revoked = true
        revokedAt = Instant.now()
    }
}
