package com.jbh.iam.gateway

import com.jbh.gateway.client.JbhGatewayClientBuilder
import com.jbh.gateway.client.JbhGatewayException
import com.jbh.gateway.client.notifications.JbhNotificationGatewayClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired


@Component
class GatewayClientFactoryImpl @Autowired constructor(
    private val gatewayClientBuilder: GatewayClientBuilder
) : GatewayClientFactory {

    companion object {
        private val LOG = LoggerFactory.getLogger(GatewayClientFactoryImpl::class.java)
    }

    private val jbhGatewayClient: JbhGatewayClientBuilder = gatewayClientBuilder.buildGatewayClient()
    
    @Volatile
    private var notificationsGatewayClient: JbhNotificationGatewayClient? = null



    override fun getNotificationClient(): JbhNotificationGatewayClient {
        return notificationsGatewayClient?: run {
            try {
                val client = jbhGatewayClient.notificationClient
                notificationsGatewayClient = client
                client
            } catch (e: JbhGatewayException) {
                LOG.error("Error while creating notifications API gateway client", e)
                throw IllegalArgumentException("Error while creating notifications" +
                        " API gateway client", e)
            }
        }
    }
}