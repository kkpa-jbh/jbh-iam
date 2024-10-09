package com.jbh.iam.viewgateway.config

import org.apache.hc.client5.http.cookie.BasicCookieStore
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {
    @Bean
    fun restTemplate(): RestTemplate {
        val cookieStore = BasicCookieStore()
        // Create HttpClient5 with cookie support
        val httpClient: CloseableHttpClient = HttpClients.custom()
            .setDefaultCookieStore(cookieStore)
            .build()

        // Create ClientHttpRequestFactory using HttpClient5
        val requestFactory = HttpComponentsClientHttpRequestFactory(httpClient)

        return RestTemplate(requestFactory)
    }

}