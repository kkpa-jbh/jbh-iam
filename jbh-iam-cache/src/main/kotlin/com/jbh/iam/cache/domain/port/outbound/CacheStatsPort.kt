package com.jbh.iam.cache.domain.port.outbound

import com.jbh.iam.cache.domain.model.CacheStats

interface CacheStatsPort {
    fun getStats(): CacheStats
    fun getMaxSize(): Long
}
