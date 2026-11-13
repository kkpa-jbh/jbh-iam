package com.jbh.iam.gateway

import com.jbh.gateway.client.JbhGatewayClientBuilder
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
class GatewayClientBuilder(
    private val configProvider: JbhGatewayClientConfigProvider,
    // Receives the circuit breaker metrics of the gateway client (/actuator/prometheus).
    private val meterRegistry: MeterRegistry,
) {
    fun buildGatewayClient(): JbhGatewayClientBuilder {
        log.info("Building Gateway Client with base URL {}", configProvider.baseUrl())
        return JbhGatewayClientBuilder.builder()
            .baseUrl(configProvider.baseUrl())
            .sourceService(ACCOUNT_SERVICE_CLIENT)
            .meterRegistry(meterRegistry)
            .build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(GatewayClientBuilder::class.java)
        private const val ACCOUNT_SERVICE_CLIENT = "JBH_IAM_API"
    }
}
