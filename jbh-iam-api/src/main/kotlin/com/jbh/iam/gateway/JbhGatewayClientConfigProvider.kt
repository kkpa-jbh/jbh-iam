package com.jbh.iam.gateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

interface JbhGatewayClientConfigProvider {
    fun baseUrl(): String?
}

@Component
class JbhGatewayClientConfigProviderImpl(
    @Value("\${jbh.gateway.base-url}") private val gatewayBaseUrl: String
) : JbhGatewayClientConfigProvider {
    override fun baseUrl(): String = gatewayBaseUrl
}
