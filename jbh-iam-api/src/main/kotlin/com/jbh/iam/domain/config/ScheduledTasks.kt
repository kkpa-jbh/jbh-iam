package com.jbh.iam.domain.config

import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.domain.modules.user.service.RefreshTokenService
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
class ScheduledTasks(
    private val refreshTokenService: RefreshTokenService
) {

    private val log = customLogger()

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    fun cleanupExpiredRefreshTokens() {
        log.info("Starting scheduled cleanup of expired refresh tokens")
        val count = refreshTokenService.cleanupExpiredTokens()
        log.info("Cleanup completed: removed $count expired tokens")
    }
}
