package com.jbh.iam.gateway

import com.jbh.gateway.client.notifications.JbhNotificationGatewayClient

interface GatewayClientFactory {
    fun getNotificationClient(): JbhNotificationGatewayClient
}