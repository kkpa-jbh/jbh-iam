# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

jbh-iam is a modular monolith IAM (Identity and Access Management) service for JBH applications, built with Kotlin, Spring Boot 3.2.0, and Gradle. It centralizes authentication, authorization, and user management using JWT tokens.

## Technology Stack

- Language: Kotlin 1.9.20 with JVM target 17
- Framework: Spring Boot 3.2.0 with Spring Cloud 2023.0.0
- Database: PostgreSQL with Liquibase for migrations
- Security: JWT-based authentication with refresh token support
- Service Discovery: Consul
- Build Tool: Gradle with Kotlin DSL
- Architecture: Pure REST API (no frontend/templates)

## Module Architecture

This is a multi-module Gradle project with clear separation of concerns:

### jbh-iam-api (Main Application)
- **Executable module** - Contains the main class `com.jbh.iam.api.JBHIAMApplication`
- REST API endpoints and controllers (`com.jbh.iam.api`)
- Domain logic with modular structure (`com.jbh.iam.domain.modules`)
- JPA repositories, entities, and services
- Liquibase migrations in `src/main/resources/db/changelog/`
- Application configuration in `src/main/resources/application.yml`
- Depends on: jbh-iam-common, jbh-iam-core, jbh-iam-security
- Default configuration: Port 9999, context path `/` (configurable via env vars)

### jbh-iam-security
- JWT authentication filter and token provider
- Spring Security configuration
- Custom UserDetailsService implementation
- Authentication and authorization services
- Library module (not executable)

### jbh-iam-core
- Core interfaces and domain models
- Security abstractions (`com.jbh.iam.core.security`)
- Environment abstractions (`com.jbh.iam.core.environment`)
- Access control models (`com.jbh.iam.core.access`)
- Defines contracts used by API and Security modules

### jbh-iam-common
- Shared utilities across all modules
- Common configurations, extensions, logging
- Payload classes and common code

### Dependency Flow
```
jbh-iam-api (main app)
  ├─> jbh-iam-core
  ├─> jbh-iam-security
  │     └─> jbh-iam-core
  └─> jbh-iam-common
```

## Domain Module Structure

The API module contains domain logic organized by business modules in `com.jbh.iam.domain.modules`:
- **user**: User entities, repositories, services
- **usergroup**: User group management
- **common**: Shared domain utilities

Each domain module follows a consistent structure: `model/`, `repository/`, `service/`

## Authentication Flow

### Sign In Process
1. `JwtAuthenticationFilter` intercepts requests (passes through for public endpoints)
2. `AuthController` receives request and calls `AuthenticationOperationImpl.authenticateUser`
3. `AuthenticationManager.authenticate` is invoked
4. `CustomUserDetailsService.loadUserByUsername` loads user from database via `UserDataAccess.findByNickNameOrEmail`
5. `UserPrincipal` (UserDetails) is created and stored in `SecurityContextHolder`
6. `JwtTokenProvider.generateToken` generates JWT
7. JWT token returned to client

JWT tokens support refresh token mechanism for expired token renewal.

## Common Development Commands

### Database Setup
```bash
# Create database with user (requires PASSWORD parameter)
make db-create PASSWORD=your_password

# Drop database
make db-drop

# Reset database (drop + recreate + migrate)
make db-reset PASSWORD=your_password

# Run migrations
make db-migrate

# Rollback last migration
make db-rollback

# Check migration status
make db-status

# Validate migrations
make db-validate

# Connect to database
make db-connect

# Complete development setup
make dev-setup PASSWORD=your_password
```

Database details:
- Name: `jbh_finance`
- User: `jbh_admin`
- Host: `localhost`
- Port: `5432`
- Timezone: `America/Bogota`

### Building and Testing
```bash
# Build all modules
./gradlew build
# or
make build

# Clean build artifacts
./gradlew clean
# or
make clean

# Run tests
./gradlew test
# or
make test

# Run tests for specific module
./gradlew :jbh-iam-api:test
./gradlew :jbh-iam-security:test
```

### Running the Application
```bash
# Run the application (starts jbh-iam-api)
./gradlew bootRun

# Run with test runtime classpath
./gradlew bootTestRun

# Or build and run the JAR
./gradlew build
java -jar jbh-iam-api/build/libs/jbh-iam-api-0.0.1-SNAPSHOT.jar
```

### Liquibase Operations
```bash
# Update database schema
./gradlew :jbh-iam-api:liquibaseUpdate

# Rollback migrations
./gradlew :jbh-iam-api:liquibaseRollbackCount -PliquibaseCommandValue=1

# Check migration status
./gradlew :jbh-iam-api:liquibaseStatus

# Validate migrations
./gradlew :jbh-iam-api:liquibaseValidate
```

### Dependency Management
```bash
# Generate dependency graph (useful for checking conflicts)
./gradlew generateDependencyGraph
```

## Application Configuration

Configuration file: `jbh-iam-api/src/main/resources/application.yml`

Key environment variables:
- `INSTANCE_PORT` - Server port (default: 9999)
- `INSTANCE_CONTEXT_PATH` - Context path (default: `/`)
- `INSTANCE_HOSTNAME` - Hostname (default: localhost)
- `JWT_SECRET` - JWT signing secret
- `JWT_EXPIRATION_IN_MS` - Token expiration time (default: 604800000ms = 7 days)
- `DB_NAME` - Database name (default: jbh)
- `JBH_ADMIN_PASS` - Database password
- `CONSUL_PORT` - Consul port (default: 8500)

Active profile: `local` (can be changed via `spring.profiles.active`)

Profile-specific configurations:
- `application-local.properties` - Local development settings
- `application-prod.properties` - Production settings
- `application-test.properties` - Test settings with H2 in-memory database

## Available Endpoints

Base URL: `http://localhost:9999`

### Authentication Endpoints (`/jbh-api/auth`)
- `POST /jbh-api/auth/signin` - User authentication
- `POST /jbh-api/auth/signup` - User registration
- `GET /jbh-api/auth/checkUsernameAvailability?username=...` - Check username availability
- `GET /jbh-api/auth/checkEmailAvailability?email=...` - Check email availability

### User Endpoints (`/jbh-api/users`)
- `GET /jbh-api/users/{nickName}` - Get user profile by nickname
- `GET /jbh-api/users` - Get all users
- `GET /jbh-api/users/userinfo` - Get authenticated user info (requires JWT token in Authorization header)
- `GET /jbh-api/users/find-user-id` - Get user ID from JWT token

### Logout Endpoint
- `POST /jbh-api/auth/logout` - Logout and invalidate session

### Actuator Endpoints
- `GET /actuator/health` - Health check endpoint (used by Consul)
- `GET /actuator/*` - Other actuator endpoints (exposed based on configuration)

## Build Configuration Notes

- **Root project**: Coordinates all subprojects, not executable
- **Subprojects**: By default, `bootJar` is disabled and `jar` is enabled (library mode)
- **jbh-iam-api**: Only module with `bootJar` enabled (creates executable JAR with embedded Tomcat)
  - Main class: `com.jbh.iam.api.JBHIAMApplication`
- **jbh-iam-security, jbh-iam-core, jbh-iam-common**: Library modules consumed by jbh-iam-api
- All modules use JVM target 17 and Kotlin 1.9.20

## Database Schema Management

- Schema managed by Liquibase (NOT by Hibernate DDL auto)
- Migration files: `jbh-iam-api/src/main/resources/db/changelog/`
- Hibernate DDL auto is set to `none`
- Always use Liquibase for schema changes

## Testing Strategy

- JUnit Platform used for all tests
- Run tests for specific modules when making changes to avoid full test suite
- Tests are located in standard `src/test/kotlin` directories per module