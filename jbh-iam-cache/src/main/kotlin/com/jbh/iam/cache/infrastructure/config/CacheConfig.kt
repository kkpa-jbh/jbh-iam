package com.jbh.iam.cache.infrastructure.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.jbh.iam.cache.api.CacheStatsOperations
import com.jbh.iam.cache.api.UserCacheOperations
import com.jbh.iam.cache.api.dto.CachedUserDTO
import com.jbh.iam.cache.application.service.CacheStatsService
import com.jbh.iam.cache.application.service.UserCacheService
import com.jbh.iam.cache.domain.port.outbound.CacheStatsPort
import com.jbh.iam.cache.domain.port.outbound.CacheStoragePort
import com.jbh.iam.cache.infrastructure.adapter.outbound.caffeine.CaffeineCacheStatsAdapter
import com.jbh.iam.cache.infrastructure.adapter.outbound.caffeine.CaffeineCacheStorageAdapter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(CacheProperties::class)
class CacheConfig(
    private val cacheProperties: CacheProperties
) {

    @Bean
    @ConditionalOnProperty(name = ["jbh.cache.user.enabled"], havingValue = "true", matchIfMissing = true)
    fun userCache(): Cache<UUID, CachedUserDTO> {
        return Caffeine.newBuilder()
            .maximumSize(cacheProperties.maxSize)
            .expireAfterWrite(cacheProperties.expireAfterWriteMinutes, TimeUnit.MINUTES)
            .expireAfterAccess(cacheProperties.expireAfterAccessMinutes, TimeUnit.MINUTES)
            .recordStats()
            .build()
    }

    @Bean
    @ConditionalOnProperty(name = ["jbh.cache.user.enabled"], havingValue = "true", matchIfMissing = true)
    fun emailIndexCache(): Cache<String, UUID> {
        return Caffeine.newBuilder()
            .maximumSize(cacheProperties.maxSize)
            .expireAfterWrite(cacheProperties.expireAfterWriteMinutes, TimeUnit.MINUTES)
            .expireAfterAccess(cacheProperties.expireAfterAccessMinutes, TimeUnit.MINUTES)
            .build()
    }

    @Bean
    @ConditionalOnProperty(name = ["jbh.cache.user.enabled"], havingValue = "true", matchIfMissing = true)
    fun nickNameIndexCache(): Cache<String, UUID> {
        return Caffeine.newBuilder()
            .maximumSize(cacheProperties.maxSize)
            .expireAfterWrite(cacheProperties.expireAfterWriteMinutes, TimeUnit.MINUTES)
            .expireAfterAccess(cacheProperties.expireAfterAccessMinutes, TimeUnit.MINUTES)
            .build()
    }

    @Bean
    @ConditionalOnProperty(name = ["jbh.cache.user.enabled"], havingValue = "true", matchIfMissing = true)
    fun userCacheStoragePort(userCache: Cache<UUID, CachedUserDTO>): CacheStoragePort<UUID, CachedUserDTO> {
        return CaffeineCacheStorageAdapter(userCache)
    }

    @Bean
    @ConditionalOnProperty(name = ["jbh.cache.user.enabled"], havingValue = "true", matchIfMissing = true)
    fun emailIndexStoragePort(emailIndexCache: Cache<String, UUID>): CacheStoragePort<String, UUID> {
        return CaffeineCacheStorageAdapter(emailIndexCache)
    }

    @Bean
    @ConditionalOnProperty(name = ["jbh.cache.user.enabled"], havingValue = "true", matchIfMissing = true)
    fun nickNameIndexStoragePort(nickNameIndexCache: Cache<String, UUID>): CacheStoragePort<String, UUID> {
        return CaffeineCacheStorageAdapter(nickNameIndexCache)
    }

    @Bean
    @ConditionalOnProperty(name = ["jbh.cache.user.enabled"], havingValue = "true", matchIfMissing = true)
    fun userCacheStatsPort(userCache: Cache<UUID, CachedUserDTO>): CacheStatsPort {
        return CaffeineCacheStatsAdapter(userCache, cacheProperties.maxSize)
    }

    @Bean
    @ConditionalOnProperty(name = ["jbh.cache.user.enabled"], havingValue = "true", matchIfMissing = true)
    fun userCacheOperations(
        userCacheStoragePort: CacheStoragePort<UUID, CachedUserDTO>,
        emailIndexStoragePort: CacheStoragePort<String, UUID>,
        nickNameIndexStoragePort: CacheStoragePort<String, UUID>
    ): UserCacheOperations {
        return UserCacheService(userCacheStoragePort, emailIndexStoragePort, nickNameIndexStoragePort)
    }

    @Bean
    @ConditionalOnProperty(name = ["jbh.cache.user.enabled"], havingValue = "true", matchIfMissing = true)
    fun cacheStatsOperations(userCacheStatsPort: CacheStatsPort): CacheStatsOperations {
        return CacheStatsService(userCacheStatsPort, cacheProperties.maxSize)
    }
}
