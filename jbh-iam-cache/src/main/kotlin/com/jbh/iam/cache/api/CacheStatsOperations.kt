package com.jbh.iam.cache.api

import com.jbh.iam.cache.api.dto.CacheStatsDTO

interface CacheStatsOperations {
    fun getUserCacheStats(): CacheStatsDTO
    fun getAllCacheStats(): Map<String, CacheStatsDTO>
}
