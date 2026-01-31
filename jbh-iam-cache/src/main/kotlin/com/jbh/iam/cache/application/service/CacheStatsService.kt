package com.jbh.iam.cache.application.service

import com.jbh.iam.cache.api.CacheStatsOperations
import com.jbh.iam.cache.api.dto.CacheStatsDTO
import com.jbh.iam.cache.domain.port.outbound.CacheStatsPort

class CacheStatsService(
    private val userCacheStatsPort: CacheStatsPort,
    private val maxSize: Long
) : CacheStatsOperations {

    companion object {
        private const val USER_CACHE_NAME = "users"
    }

    override fun getUserCacheStats(): CacheStatsDTO {
        val stats = userCacheStatsPort.getStats()
        val usagePercentage = if (maxSize > 0) {
            (stats.currentSize.toDouble() / maxSize) * 100
        } else 0.0

        return CacheStatsDTO(
            cacheName = USER_CACHE_NAME,
            hitCount = stats.hitCount,
            missCount = stats.missCount,
            hitRate = stats.hitRate * 100,
            evictionCount = stats.evictionCount,
            currentSize = stats.currentSize,
            maxSize = maxSize,
            usagePercentage = usagePercentage
        )
    }

    override fun getAllCacheStats(): Map<String, CacheStatsDTO> {
        return mapOf(USER_CACHE_NAME to getUserCacheStats())
    }
}
