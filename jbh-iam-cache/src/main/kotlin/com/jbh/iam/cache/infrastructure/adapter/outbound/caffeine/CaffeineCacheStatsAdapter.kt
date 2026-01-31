package com.jbh.iam.cache.infrastructure.adapter.outbound.caffeine

import com.github.benmanes.caffeine.cache.Cache
import com.jbh.iam.cache.domain.model.CacheStats
import com.jbh.iam.cache.domain.port.outbound.CacheStatsPort

class CaffeineCacheStatsAdapter<K : Any, V>(
    private val cache: Cache<K, V>,
    private val maxSize: Long
) : CacheStatsPort {

    override fun getStats(): CacheStats {
        val stats = cache.stats()
        return CacheStats(
            hitCount = stats.hitCount(),
            missCount = stats.missCount(),
            evictionCount = stats.evictionCount(),
            currentSize = cache.estimatedSize()
        )
    }

    override fun getMaxSize(): Long = maxSize
}
