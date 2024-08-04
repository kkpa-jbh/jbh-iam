package com.jbh.iam.api.trying

import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ShutDownDependency constructor(@Autowired private val shutDownHookService: ShutDownHookService) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ShutDownDependency::class.java)
    }

    @PreDestroy
    fun releaseAllLockedEntities() {
        for (lock in LockedEntities.lockedConnections.keys()) {
            log.info("Releasing lock2 $lock")
            LockedEntities.peek(lock)
        }
        log.info("Size LockedConnections2 ${LockedEntities.lockedConnections.size}")
    }
}