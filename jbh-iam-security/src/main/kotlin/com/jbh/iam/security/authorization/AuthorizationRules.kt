package com.jbh.iam.security.authorization

import org.slf4j.LoggerFactory
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.stereotype.Component

@Component
class AuthorizationRules :
    Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> {

    companion object {
        private val log = LoggerFactory.getLogger(AuthorizationRules::class.java)
        const val IAM_BASE_API_PATH = "/jbh-api"
        const val AUTH_PATH = "/auth"

        val AUTH_API_WHITELIST = arrayOf(
            IAM_BASE_API_PATH + AUTH_PATH + "/**",
            "/actuator/**",
        )

        val AUTH_VIEW_WHITELIST = arrayOf(
            "/view/auth/**",
        )

        val STATIC_RESOURCES = arrayOf(
            "/static/**",
            "/js/**",
            "/css/**",
            "/images/**",
            "/docs/**",
            "/swagger-ui.html",
        )

    }


    override fun customize(registry: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        log.info("Registering JBH authorization rules")

        registry
            .requestMatchers(*STATIC_RESOURCES).permitAll()
            .requestMatchers(*AUTH_API_WHITELIST).permitAll()
            .requestMatchers(*AUTH_VIEW_WHITELIST).permitAll()
            .requestMatchers("/admin/**").hasAnyAuthority("ROLE_ADMIN")
            .requestMatchers("/user/**").hasAuthority("ROLE_USER")
            .anyRequest().authenticated()

        log.info("Authorization rules registered successfully")
    }
}