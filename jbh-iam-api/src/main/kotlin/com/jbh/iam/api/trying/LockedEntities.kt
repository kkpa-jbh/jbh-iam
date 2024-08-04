package com.jbh.iam.api.trying

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

object LockedEntities {

    private val log: Logger = LoggerFactory.getLogger(LockedEntities::class.java)
    val lockedConnections = ConcurrentHashMap<String, Boolean>()

    fun push(entityLocked: String): Int {
        lockedConnections.putIfAbsent(entityLocked, true)
        val currentSize = lockedConnections.size
        log.error("Putting.... $entityLocked - $currentSize")
        return currentSize
    }

    fun peek(entityLocked: String): Int {
        val result = lockedConnections.remove(entityLocked)
        val currentSize = lockedConnections.size
        log.error("Removing.... $entityLocked - $result - $currentSize")
        return lockedConnections.size
    }
}