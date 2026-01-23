# jbh-iam

Modular monolith REST API for handling IAM (Identity and Access Management) in JBH.

This project is designed to centralize authentication, authorization, and user management for JBH applications.

It provides a pure REST API for IAM operations using Kotlin, Spring Boot, and Gradle with JWT-based authentication.

The structure supports code reuse, separation of concerns, and scalability.

## Modules

**jbh-iam-api** (Main Application): The executable Spring Boot application containing API endpoints, controllers, DTOs, domain logic, and error handling. This is the entry point that runs the IAM service.

**jbh-iam-common**: Shared code and utilities (e.g., config, extensions, logging, payloads) used across other modules.

**jbh-iam-core**: Core business logic, domain models, and security/environment abstractions for IAM.

**jbh-iam-security**: Handles authentication and security concerns, providing JWT authentication mechanisms and security filters.

# How to use it?

## Server Configuration
- Port: 9999 (configurable via `INSTANCE_PORT` env var)
- Context Path: `/` (configurable via `INSTANCE_CONTEXT_PATH` env var)
- Base URL: http://localhost:9999

## Running the Application
```bash
# Using Gradle
./gradlew bootRun

# Or build and run the JAR
./gradlew build
java -jar jbh-iam-api/build/libs/jbh-iam-api-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Authentication Endpoints (`/jbh-api/auth`)
- `POST /jbh-api/auth/signin` - User authentication
- `POST /jbh-api/auth/signup` - User registration
- `GET /jbh-api/auth/checkUsernameAvailability?username=...` - Check username availability
- `GET /jbh-api/auth/checkEmailAvailability?email=...` - Check email availability

### User Endpoints (`/jbh-api/users`)
- `GET /jbh-api/users/{nickName}` - Get user profile by nickname
- `GET /jbh-api/users` - Get all users
- `GET /jbh-api/users/userinfo` - Get authenticated user info (requires JWT token)
- `GET /jbh-api/users/find-user-id` - Get user ID from JWT token

### Logout Endpoint
- `POST /jbh-api/logout` - Logout and invalidate session

## Environment Variables
- `INSTANCE_PORT`: Server port (default: 9999)
- `INSTANCE_CONTEXT_PATH`: Application context path (default: `/`)
- `INSTANCE_HOSTNAME`: Hostname (default: localhost)
- `DB_NAME`: Database name (default: jbh)
- `JBH_ADMIN_PASS`: Database password
- `JWT_SECRET`: JWT signing secret (default: JWTSuperSecretKey)
- `JWT_EXPIRATION_IN_MS`: Token expiration time in milliseconds (default: 604800000 = 7 days)
- `CONSUL_PORT`: Consul service discovery port (default: 8500)