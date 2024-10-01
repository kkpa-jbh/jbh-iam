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
        val AUTH_API_WHITELIST = arrayOf(
            "/iam-api/auth/**",
            "/auth/**",
            "/actuator/",
            "/auth/signin",
            "/auth/checkUsernameAvailability",
            "/auth/checkEmailAvailability",
            "/auth/signup",

            )

        val AUTH_VIEW_WHITELIST = arrayOf(
            "/jbh/view/auth/",
        )

        val STATIC_RESOURCES = arrayOf(
            "/css/**",
            "/js/**",
            "/img/**",
            "/images/**",
            "/",
            "/favicon.ico",
            "/**/*.png",
            "/**/*.gif",
            "/**/*.svg",
            "/**/*.jpg",
            "/**/*.html",
            "/**/*.css",
            "/**/*.js"
        )

    }

    private val AUTH_WHITELIST = arrayOf(
        // -- swagger ui
        "/v3/api-docs",
        "/swagger-resources/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/service",
        "/actuator/**",
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
            .requestMatchers(*AUTH_WHITELIST).permitAll()
            .requestMatchers(*AUTH_API_WHITELIST).permitAll()
            .requestMatchers("/view/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/party-deserialization/**").permitAll()
            .requestMatchers("/admin/**").hasAnyAuthority("ROLE_ADMIN")
            .requestMatchers("/user/**").hasAuthority("ROLE_USER")
            .anyRequest().authenticated()

        log.info("Authorization rules registered successfully")
    }
}