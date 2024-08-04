package com.jbh.iam.api.trying

import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantLock

@Service
class ShutDownHookService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ShutDownHookService::class.java)
    }

    val lock by lazy {
        ReentrantLock()
    }

    @PreDestroy
    fun releaseAllLockEntities() {
        lock.lock()
        try {
            for (lock in LockedEntities.lockedConnections.keys()) {
                log.info("Releasing lock $lock by ${Thread.currentThread().name}")
                LockedEntities.peek(lock)
                Thread.sleep(200L)
            }
            log.info("Size LockedConnections ${LockedEntities.lockedConnections.size}")
        } finally {
            lock.unlock()
        }
    }
}