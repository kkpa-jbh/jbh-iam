package com.jbh.iam.security.config


import com.jbh.iam.security.authentication.JwtAuthenticationFilter
import com.jbh.iam.security.authorization.AuthorizationRules
import com.jbh.iam.security.service.CustomUserDetailsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*


@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true) // Annotation-based security (like @PreAuthorize).
@EnableWebSecurity // This is the primary spring security annotation that is used to enable web security in a project.
class SecurityConfig() {

    var securityDebug = false

    @Autowired
    lateinit var jbhAuthorizer: AuthorizationRules

    // Comma-separated in app.corsAllowedOrigins (env CORS_ALLOWED_ORIGINS).
    @Value("\${app.corsAllowedOrigins}")
    lateinit var corsAllowedOrigins: List<String>

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
        // No .and(): it is deprecated since Spring Security 6.1 and removed in 7.
        val builder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        builder.userDetailsService(userDetailService).passwordEncoder(bCryptPasswordEncoder)
        return builder.build()
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
                    *AuthorizationRules.STATIC_RESOURCES
                )
        }
    }


    /**
     * Configures CORS (Cross-Origin Resource Sharing) for the application.
     *
     * This configuration is CRITICAL for allowing the Angular/Ionic frontend to:
     * 1. Make cross-origin requests to the backend API
     * 2. Send and receive cookies (JWT and refresh tokens) across origins
     *
     * Key settings:
     * - allowCredentials = true: REQUIRED for cookies to work across origins
     * - allowedOrigins: from app.corsAllowedOrigins (env CORS_ALLOWED_ORIGINS)
     *   - Local development default: http://localhost:4200, http://localhost:8100 (Ionic), and the 127.0.0.1 forms
     *   - Production: https://jbh.usemagus.cloud. The app and the API share that host, but behind Caddy and the
     *     gateway IAM sees another host, so Spring still treats the browser's Origin as cross-origin.
     * - allowedMethods: HTTP methods the frontend can use
     * - allowedHeaders: Headers the frontend can send (Authorization, Content-Type, etc.)
     * - exposedHeaders: Headers the frontend can read from responses
     *
     * SECURITY NOTE: never add "*" — allowCredentials = true requires explicit origins.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        log.info("Configuring CORS for cross-origin cookie support")
        val configuration = CorsConfiguration()

        // Allow credentials (cookies, authorization headers) to be sent with requests
        // This MUST be true for HttpOnly cookies to work across origins
        configuration.allowCredentials = true

        // Allowed origins: frontend URLs that can access this API (see KDoc above)
        configuration.allowedOrigins = corsAllowedOrigins.map { it.trim() }.filter { it.isNotEmpty() }
        log.info("CORS allowed origins: {}", configuration.allowedOrigins)

        // Allow all standard HTTP methods
        configuration.allowedMethods = listOf(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS",
            "PATCH"
        )

        // Allow all headers commonly used by frontends
        // Including Authorization for JWT tokens sent in headers (if needed as fallback)
        configuration.allowedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Requested-With"
        )

        // Expose headers that the frontend JavaScript can read from responses
        configuration.exposedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "Set-Cookie"
        )

        // Cache preflight requests for 1 hour to reduce OPTIONS requests
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    /**
     * Configures the security filter chain for the application.
     *
     * This method sets up the main security configuration, including:
     * - CORS (Cross-Origin Resource Sharing) for Angular/Ionic frontend
     * - CSRF (Cross-Site Request Forgery) protection (disabled for stateless JWT)
     *   CSRF protection is primarily needed for session-based authentication.
     *   In a stateless JWT-based system, the token itself provides protection.
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
            // Enable CORS with the configuration from corsConfigurationSource()
            // This MUST come before other configurations to handle preflight OPTIONS requests
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource())
            }
            // Disable CSRF for stateless JWT authentication
            .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
            .authorizeHttpRequests(jbhAuthorizer)
            .addFilterAfter(
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

