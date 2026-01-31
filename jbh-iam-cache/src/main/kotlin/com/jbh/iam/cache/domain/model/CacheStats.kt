package com.jbh.iam.cache.domain.model

data class CacheStats(
    val hitCount: Long,
    val missCount: Long,
    val evictionCount: Long,
    val currentSize: Long
) {
    val requestCount: Long get() = hitCount + missCount
    val hitRate: Double get() = if (requestCount > 0) hitCount.toDouble() / requestCount else 0.0
}
