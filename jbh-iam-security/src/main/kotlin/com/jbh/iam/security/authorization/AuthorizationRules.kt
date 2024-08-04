package com.jbh.iam.security.authorization

import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.stereotype.Component

@Component
class AuthorizationRules :
    Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> {

    companion object {
        private val log = LoggerFactory.getLogger(AuthorizationRules::class.java)
    }

    private val AUTH_WHITELIST = arrayOf(
        // -- swagger ui
        "/v3/api-docs",
        "/swagger-resources/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/service",
        "/actuator/**",
        "/checkService",
        "/configuration/ui",
        "/configuration/security",
        "/static/**",
        "/js/**",
        "/css/**",
        "/images/**",
        "/webjars/**"
    )

    override fun customize(registry: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry) {
        log.info("Registering JBH authorization rules")

        registry
            //.requestMatchers(*AUTH_WHITELIST).permitAll()
            .requestMatchers("/jbh/auth/signin").permitAll()  // Specific rule for signin
            .requestMatchers("/jbh/auth/signup").permitAll()  // Specific rule for signin
            .requestMatchers("/jbh/auth/**").permitAll()
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/party-deserialization/**").permitAll()
            .requestMatchers("/admin/**").hasAnyAuthority("ROLE_ADMIN")
            .requestMatchers("/user/**").hasAuthority("ROLE_USER")
            .anyRequest().authenticated()

        log.info("Authorization rules registered successfully")
    }
}