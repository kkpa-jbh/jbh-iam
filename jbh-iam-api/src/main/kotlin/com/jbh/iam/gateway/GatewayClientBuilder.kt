package com.jbh.iam.gateway

import com.jbh.gateway.client.JbhGatewayClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
class GatewayClientBuilder  constructor(private val configProvider: JbhGatewayClientConfigProvider) {
    fun buildGatewayClient(): JbhGatewayClientBuilder {
        println("Building Gateway Client...${configProvider.baseUrl()}")
        return JbhGatewayClientBuilder.builder()
            .baseUrl(configProvider.baseUrl())
            .sourceService(ACCOUNT_SERVICE_CLIENT)
            .build()
    }

    companion object {
        private const val ACCOUNT_SERVICE_CLIENT = "JBH_IAM_API"
    }
}
