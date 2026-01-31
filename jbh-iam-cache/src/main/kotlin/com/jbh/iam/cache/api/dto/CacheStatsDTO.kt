package com.jbh.iam.cache.api.dto

data class CacheStatsDTO(
    val cacheName: String,
    val hitCount: Long,
    val missCount: Long,
    val hitRate: Double,
    val evictionCount: Long,
    val currentSize: Long,
    val maxSize: Long,
    val usagePercentage: Double
)
