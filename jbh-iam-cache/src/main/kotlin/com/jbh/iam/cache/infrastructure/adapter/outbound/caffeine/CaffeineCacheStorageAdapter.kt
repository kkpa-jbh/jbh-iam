package com.jbh.iam.cache.infrastructure.adapter.outbound.caffeine

import com.github.benmanes.caffeine.cache.Cache
import com.jbh.iam.cache.domain.port.outbound.CacheStoragePort

class CaffeineCacheStorageAdapter<K : Any, V>(
    private val cache: Cache<K, V>
) : CacheStoragePort<K, V> {

    override fun get(key: K): V? = cache.getIfPresent(key)

    override fun put(key: K, value: V) {
        cache.put(key, value)
    }

    override fun remove(key: K): V? {
        val value = cache.getIfPresent(key)
        cache.invalidate(key)
        return value
    }

    override fun clear() {
        cache.invalidateAll()
    }

    override fun size(): Long = cache.estimatedSize()

    override fun containsKey(key: K): Boolean = cache.getIfPresent(key) != null
}
