package com.jbh.iam.security.config


import com.jbh.iam.security.authentication.JwtAuthenticationFilter
import com.jbh.iam.security.authorization.AuthorizationRules
import com.jbh.iam.security.service.CustomUserDetailsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.*


@Configuration
@EnableMethodSecurity
@EnableWebSecurity // This is the primary spring security annotation that is used to enable web security in a project.
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
) // : Enables annotation-based security (like @PreAuthorize).
class SecurityConfig() {

    var securityDebug = false

    @Autowired
    lateinit var jbhAuthorizer: AuthorizationRules

    companion object {
        private val log = LoggerFactory.getLogger(SecurityConfig::class.java)
    }


    /**
     * The Authentication Manager is responsible for validating user credentials.
     * It is used to authenticate users and provide them with a security context.
     * Configures and creates the AuthenticationManager bean.
     *
     * This method sets up the core authentication process for Spring Security.
     * It integrates the custom UserDetailsService and BCrypt password encoder
     * into the authentication flow.
     *
     * @param http The HttpSecurity object to get the shared AuthenticationManagerBuilder
     * @param bCryptPasswordEncoder The password encoder to use for password hashing
     * @param userDetailService The custom service to load user-specific data
     * @return The configured AuthenticationManager
     * @throws Exception If there's an error during AuthenticationManager creation
     */
    @Bean
    @Throws(Exception::class)
    fun authenticationManager(
        http: HttpSecurity,
        bCryptPasswordEncoder: BCryptPasswordEncoder?,
        userDetailService: CustomUserDetailsService?
    ): AuthenticationManager? {
        log.info("Creating authentication manager")
        return http.getSharedObject<AuthenticationManagerBuilder>(AuthenticationManagerBuilder::class.java)
            .userDetailsService<CustomUserDetailsService>(userDetailService)
            .passwordEncoder(bCryptPasswordEncoder)
            .and()
            .build()
    }


    /**
     *
     * The webSecurityCustomizer bean you've shown is primarily designed to handle static web resources
     **
     * It configures Spring Security to ignore certain paths, typically used for static resources.
     * This allows public access to resources like CSS, JavaScript, images, and HTML files without going through the security filters.
     * It also allows the application to serve these resources directly without going through the security filters.
     *
     * This is useful for configurations that need to be applied before the main security filter chain.
     * @see JwtAuthenticationFilter::doFilterInternal and filterChain
     *
     */
    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer? {
        log.info("Creating web security customizer")
        return WebSecurityCustomizer { web: WebSecurity ->
            web.debug(securityDebug)
                .ignoring()
                .requestMatchers(
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
    }


    /**
     * Configures the security filter chain for the application.
     *
     * This method sets up the main security configuration, including:
     * - Disabling CSRF protection
     * - Applying custom authorization rules
     * - Adding a JWT authentication filter
     * - Configuring stateless session management
     *
     * @param http The HttpSecurity object to configure
     * @return The configured SecurityFilterChain
     * @throws Exception If there's an error during configuration
     */
    @Bean
    @Throws(java.lang.Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {
        log.info("Security Filter Chain is being created")
        http
            .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
            .authorizeHttpRequests(jbhAuthorizer)
            .addFilterBefore(
                jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter::class.java
            ) // Adding to the chain.
            .sessionManagement { session: SessionManagementConfigurer<HttpSecurity?> ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            }

        return http.build()
    }

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter()
    }

}

