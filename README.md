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

**jbh-iam-cache**: In-memory user cache (`jbh.cache.user.*` settings), with `api`, `application`, `domain` and `infrastructure` layers.

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

Set `JBH_ADMIN_PASS` and `JBH_JWT_SECRET` first. `.env.example` lists them. You also need Postgres and Consul (see "Environment Variables").

## Test it
```bash
./gradlew test
```
- Unit tests need nothing else.
- `AuthFlowIntegrationTest` (sign up, sign in, refresh, reuse, logout) starts Postgres with Testcontainers. It is skipped when Docker is not running.
- Each module writes a JaCoCo report to `<module>/build/reports/jacoco/test/`. CI prints the total line coverage in the job summary.
- `./gradlew ktlintCheck` reports style problems. It does not fail the build yet.

## API Endpoints

All controllers get the `/jbh-api` prefix (`ApiWebConfig`). Path constants live in `Routes`.
Clients call these through `jbh-gateway` (port 8080), not directly.

### Authentication Endpoints (`/jbh-api/auth`) - public
- `POST /jbh-api/auth/signin` - User authentication
- `POST /jbh-api/auth/signup` - User registration
- `POST /jbh-api/auth/refresh` - Get a new access token with a refresh token
- `POST /jbh-api/auth/logout` - Logout and invalidate session
- `GET /jbh-api/auth/checkUsernameAvailability?nickName=...` - Check username availability
- `GET /jbh-api/auth/checkEmailAvailability?email=...` - Check email availability

### User Endpoints (`/jbh-api/users`)
- `GET /jbh-api/users/{nickName}` - Get user profile by nickname
- `GET /jbh-api/users/` - Get all users
- `GET /jbh-api/users/userinfo` - Get authenticated user info
- `GET /jbh-api/users/find-user-id` - Get user ID from JWT token (used by `jbh-personal-finance` through `jbh-gateway-client`)

### User Group Endpoints (`/jbh-api/user-groups`)
- `GET /jbh-api/user-groups` - All groups (ADMIN only)
- `GET /jbh-api/user-groups/{id}` - Single group
- `POST /jbh-api/user-groups` - Create group
- `PUT /jbh-api/user-groups/{id}` - Update group
- `DELETE /jbh-api/user-groups/{id}` - Delete group
- `GET /jbh-api/user-groups/my-groups` - Current user's groups
- `GET /jbh-api/user-groups/my-invitations` - Current user's pending invitations
- `POST /jbh-api/user-groups/{groupId}/invitations` - Invite user (sends an email through the notifications module)
- `POST /jbh-api/user-groups/{groupId}/reinvite` - Send the invitation again
- `POST /jbh-api/user-groups/{groupId}/invitations/accept` - Accept invitation
- `POST /jbh-api/user-groups/{groupId}/invitations/decline` - Decline invitation
- `GET /jbh-api/user-groups/{groupId}/invitations` - List invitations of a group
- `DELETE /jbh-api/user-groups/{groupId}/members/{userEmail}` - Remove a member

### Admin Endpoints
- `POST /jbh-api/admin/users/{userId}/revoke-tokens` - Revoke all tokens of a user
- `GET /jbh-api/admin/cache/stats`, `GET /jbh-api/admin/cache/stats/users` - Cache statistics

Note: `jbh-gateway` has no route for `/jbh-api/admin/**`. Admin endpoints work only when you call port 9999 directly.

## Environment Variables
- `INSTANCE_PORT`: Server port (default: 9999)
- `INSTANCE_CONTEXT_PATH`: Application context path (default: `/`)
- `INSTANCE_HOSTNAME`: Fallback host for Postgres and Consul (default: localhost)
- `DB_HOST`, `DB_PORT`, `DB_USERNAME`: Postgres host (default: `INSTANCE_HOSTNAME`), port (5432), user (`jbh_admin`)
- `CONSUL_HOST`: Consul host (default: `INSTANCE_HOSTNAME`)
- `CORS_ALLOWED_ORIGINS`: Browser origins allowed by CORS, comma separated (default: the localhost dev servers; production: `https://jbh.usemagus.cloud`)
- `CONSUL_DISCOVERY_HOSTNAME`: Name registered in Consul, used by the gateway to reach IAM (default: localhost; Docker: `jbh-iam`)
- `DB_NAME`: Database name (default: `jbh`, the same name `make db-create` creates)
- `JBH_ADMIN_PASS`: Database password (user `jbh_admin`). **Required**, no default.
- `JBH_JWT_SECRET`: JWT signing secret, Base64, at least 32 bytes (`openssl rand -base64 32`). **Required**, no default: IAM stops at start without it. Must be the same value in `jbh-gateway`.
- `JWT_EXPIRATION_IN_MS`: Access token expiration (default: 900000 = 15 minutes)
- `REFRESH_TOKEN_EXPIRATION_IN_MS`: Refresh token expiration (default: 2592000000 = 30 days)
- `GOOGLE_CLIENT_IDS`: Google OAuth client IDs for Google sign-in, comma separated (web, android, ios). Default: the JBH web client ID. An empty value turns Google sign-in off. The web ID must match `google.clientId` in the `jbh-app` environment files.
- `CONSUL_PORT`: Consul service discovery port (default: 8500)
- `JBH_GATEWAY_URL`: Gateway URL used by `jbh-gateway-client` (default: http://localhost:8080)
- `CACHE_USER_*`: User cache size and expiration (see `application.yml`)

## Decisions and trade-offs
- **HS256 JWT with a secret shared with `jbh-gateway`**, instead of a public/private key pair. It is simple, and the gateway can validate tokens without calling IAM. The cost: the secret must stay identical in both services, and anyone who has it can create tokens.
- **Refresh tokens are stored as SHA-256 hashes and rotated on every use.** One database update (`revokeIfActive`) decides which parallel call wins. A reused token gets `401`.
- **Liquibase owns the schema** (`ddl-auto: none`). Changes are explicit and reviewed, but every model change needs a changelog.
- **In-memory user cache** (`jbh-iam-cache`) instead of Redis. It has no extra service to run. The cost: each instance has its own cache, so a running second instance can show stale data until entries expire.
- **Calls to other services go through `jbh-gateway`** with `jbh-gateway-client`, not directly. One place checks tokens and applies rate limits.

## Limits / next steps
- There is no OpenAPI spec. The endpoint tables above are the only API doc.
- ktlint does not fail the build yet, and many files use wildcard imports.
- Early commits contain old development credentials. Do not reuse them anywhere.
- Admin endpoints are not routed by `jbh-gateway`. Call port 9999 directly.

## License
MIT. See `LICENSE`.
