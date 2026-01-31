package com.jbh.iam.cache.api

interface CacheAdapter<K, V> {
    fun get(key: K): V?
    fun getOrLoad(key: K, loader: () -> V?): V?
    fun put(key: K, value: V)
    fun evict(key: K)
    fun evictAll()
    fun refresh(key: K, loader: () -> V?)
}
