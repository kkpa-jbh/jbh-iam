package com.jbh.iam.api.components.admin

import com.jbh.iam.api.Routes
import com.jbh.iam.cache.api.CacheStatsOperations
import com.jbh.iam.cache.api.dto.CacheStatsDTO
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.core.access.AuthenticationOperationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(Routes.ADMIN_CACHE_PATH)
class CacheStatsController(
    private val cacheStatsOperations: CacheStatsOperations,
    private val authenticationOperationService: AuthenticationOperationService
) {

    private val log = customLogger()

    @GetMapping("/stats")
    fun getAllCacheStats(): ResponseEntity<Map<String, CacheStatsDTO>> {
        authenticationOperationService.requireAdminRole()
        log.info("Admin requesting cache statistics")
        return ResponseEntity.ok(cacheStatsOperations.getAllCacheStats())
    }

    @GetMapping("/stats/users")
    fun getUserCacheStats(): ResponseEntity<CacheStatsDTO> {
        authenticationOperationService.requireAdminRole()
        log.info("Admin requesting user cache statistics")
        return ResponseEntity.ok(cacheStatsOperations.getUserCacheStats())
    }
}
