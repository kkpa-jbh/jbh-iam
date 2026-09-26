# Module Structure

## jbh-iam-api

It's the main application module (pure REST API, no web views).

Purpose: REST controllers, DTOs, domain modules (`user`, `usergroup`), JPA, Liquibase and the Spring Boot main class.
It also calls other services through `jbh-gateway` with the `jbh-gateway-client` library (package `com.jbh.iam.gateway`).

## jbh-iam-cache

In-memory user cache (`jbh.cache.user.*` settings).

## jbh-iam-common

Purpose: Contains shared code, utilities, and common models used across other modules.
Key components: Likely includes basic data classes, utility functions, and possibly interface definitions.

## jbh-iam-core

This module defines interfaces and domain models used by both API and security modules.

### Dependencly flow

API Module → Core Module ← Security Module

## jbh-iam-security

Purpose: Handles security configurations, JWT processing, and authentication/authorization logic.
Key components: SecurityConfig, JwtAuthenticationFilter, and other security-related classes.

### Project Structure

#### authentication package:

JwtAuthenticationEntryPoint: Handles authentication errors
JwtAuthenticationFilter: Intercepts requests to validate JWT tokens
JwtTokenProvider: Generates and validates JWT tokens

#### config package:

SecurityConfig: Main security configuration class
AuthorizationCustomizer: Customizes authorization rules

#### model package:

CurrentUser: Represents the currently authenticated user

#### service package:

AuthenticationServiceImpl: Implements authentication operations (likely an interface from iam-core)
CustomUserDetailsService: Loads user-specific data for authentication
PasswordEncoderImpl: Implements password encoding (likely an interface from iam-core)

#### Tree-like package structure

```
com.jbh.iam.security
├── authentication
│   ├── filter
│   │   └── JwtAuthenticationFilter
│   ├── service
│   │   └── AuthenticationServiceImpl
│   ├── JwtAuthenticationEntryPoint
│   └── JwtTokenProvider
├── authorization
│   └── AuthorizationRules
├── config
│   └── SecurityConfig
├── model
│   └── CurrentUser
├── user
│   ├── CustomUserDetailsService
│   └── PasswordEncoderImpl
└── util
    └── JwtUtil (if needed)
```

## jbh-iam-api

Purpose: Contains the main application logic, controllers, services, and data access layer.
Key components: REST controllers, service implementations, repositories, and entity classes.

# Flow

## Sign In

1. [Security] `JwtAuthenticationFilter` intercepts the request and let the request pass because it's a public endpoint.
2. [Api] Enters the `AuthController` and calls `AuthenticationOperationImpl.authenticateUser` method.
3. [Security] Calls `AuthenticationManager.authenticate` method.
    - [Security] Calls `CustomUserDetailsService.loadUserByUsername` method.
    - [Api] Calls `UserDataAccess.findByNickNameOrEmail` method.
    - Creates a `UserPrincipal` as `UserDetails` object and returns it.
    - The `UserPrincipal` is stored in `SecurityContextHolder.getContext().authentication` object.
        - The `UserPrincipal` is an instance of `UsernamePasswordAuthenticationToken` class.
            - Contains the principal object, `authorities` object and flag `isAuthenticated` as `true`.
    - [Security] Calls `JwtTokenProvider.generateToken` method.
    - Returns the JWT token.
4. The AuthController returns the JWT token to the client.
