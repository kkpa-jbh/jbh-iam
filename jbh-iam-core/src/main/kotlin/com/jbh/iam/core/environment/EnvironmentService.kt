package com.jbh.iam.core.environment

import com.jbh.iam.common.logging.customLogger
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class EnvironmentService @Autowired constructor(private val environment: Environment) {

    private val log = customLogger()

    @PostConstruct
    fun init() {
        log.info("Running in ${getCurrentEnvironment()} environment")
        if (isProd()) {
            log.info("Finally Running in production, Congrats!")
        }
    }

    fun isLocal(): Boolean = environment.activeProfiles.contains(PROFILE_LOCAL)

    fun isSandbox(): Boolean = environment.activeProfiles.contains(PROFILE_SANDBOX)

    fun isProd(): Boolean = environment.activeProfiles.contains(PROFILE_PROD)

    fun getCurrentEnvironment(): String = when {
        isLocal() -> PROFILE_LOCAL
        isSandbox() -> PROFILE_SANDBOX
        isProd() -> PROFILE_PROD
        else -> "Unknown"
    }

    companion object {
        const val PROFILE_LOCAL = "local"
        const val PROFILE_SANDBOX = "sandbox"
        const val PROFILE_PROD = "prod"
    }
}