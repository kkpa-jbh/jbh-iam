package com.jbh.iam.cache.domain.port.outbound

interface CacheStoragePort<K, V> {
    fun get(key: K): V?
    fun put(key: K, value: V)
    fun remove(key: K): V?
    fun clear()
    fun size(): Long
    fun containsKey(key: K): Boolean
}
