package com.jbh.iam.cache.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jbh.cache.user")
data class CacheProperties(
    val maxSize: Long = 5000,
    val expireAfterWriteMinutes: Long = 60,
    val expireAfterAccessMinutes: Long = 30,
    val enabled: Boolean = true
)
