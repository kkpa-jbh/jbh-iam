package com.jbh.iam.gateway

import com.jbh.gateway.client.JbhGatewayClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
class GatewayClientBuilder  constructor(private val configProvider: JbhGatewayClientConfigProvider) {
    fun buildGatewayClient(): JbhGatewayClientBuilder {
        log.info("Building Gateway Client with base URL {}", configProvider.baseUrl())
        return JbhGatewayClientBuilder.builder()
            .baseUrl(configProvider.baseUrl())
            .sourceService(ACCOUNT_SERVICE_CLIENT)
            .build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(GatewayClientBuilder::class.java)
        private const val ACCOUNT_SERVICE_CLIENT = "JBH_IAM_API"
    }
}
