# AGENTS.md - jbh-iam

Instructions for AI agents (Codex, Claude Code, others) that change or review this repository.
Threat model and security rules: [`SECURITY.md`](SECURITY.md).

## What this service is

- IAM for JBH: sign-in, sign-up, Google sign-in, JWT access tokens, refresh tokens, users, user groups.
- Kotlin 1.9, Spring Boot 3.5, Gradle multi-module, Postgres with Liquibase, Consul for discovery.
- One service in a microservice system. Clients reach it only through `jbh-gateway` (`/jbh-api/**`).
  System map: [jbh-deploy README](https://github.com/kkpa-jbh/jbh-deploy/blob/main/README.md).
- Stateless. It must run as N identical instances behind the gateway (Consul load balancing).
  Any instance can serve any request. Any instance can stop at any time.

## Modules

| Module | Job |
|--------|-----|
| `jbh-iam-api` | The Spring Boot app: controllers, domain modules (`user`, `usergroup`), JPA, Liquibase |
| `jbh-iam-security` | Spring Security config, JWT filter and token provider. The only module with Spring Security |
| `jbh-iam-core` | Interfaces and domain models shared by `api` and `security` |
| `jbh-iam-common` | Shared config, constants, logging, payloads, `ForbiddenException` |
| `jbh-iam-cache` | In-memory user cache (Caffeine), one per instance |

## Commands

- All unit tests: `./gradlew test`. They need no Postgres, Consul, or network.
- One module: `./gradlew :jbh-iam-api:test`.
- `jbh-gateway-client` comes from GitHub Packages. Set `GITHUB_ACTOR` and `PACKAGES_READ_TOKEN`,
  or install the library in `~/.m2` first.

## Conventions

- Paths live in `Routes`. `ApiWebConfig` adds the `/jbh-api` prefix to every controller.
- Schema changes: a new Liquibase changeset in `jbh-iam-api/src/main/resources/db/changelog/`.
  Never edit a changeset that already ran. Hibernate `ddl-auto` stays `none`.
- A denied action throws `ForbiddenException` (`jbh-iam-common`). `GlobalErrorHandler` answers `403`.
- Config comes from env vars. Secrets (`JBH_JWT_SECRET`, `JBH_ADMIN_PASS`) have no default.
- Calls to other services go only through `jbh-gateway-client` (`GatewayClientFactory`).
- No frontend code (HTML, CSS, JS, templates).

## Code Review Rules

Report only problems that should block a merge.

- **P0**: an exploitable security problem, a token or secret leak, or data loss.
- **P1**: a broken auth contract, a break for another service, or a bug that appears only with more than one instance.
- Do not report style, naming, formatting, refactors, or missing tests on their own.
- Do not report a problem in code that the pull request did not change, unless the change makes it reachable.

### Tokens and secrets

- Never log or return passwords, password hashes, JWTs, refresh tokens, Google ID tokens, or the `Authorization` header.
  Safe path: log the user id.
- Refresh tokens: store only the SHA-256 hash. Rotate and revoke with the conditional update `revokeIfActive(...) == 1`.
  Flag any read-then-write ("find token, check `revoked`, save"). Two instances could then accept the same token.
- The access token is a contract with `jbh-gateway`: HS256 with `JBH_JWT_SECRET`, claims `sub` (user id) and
  `token_type=ACCESS`, cookies `JBH_TOKEN` / `__Secure-JBH_TOKEN`. Changing any of these is P1,
  unless the pull request says the gateway changes too.
- Token cookies stay `HttpOnly` and `SameSite=Lax`, and `Secure` in production.
- Never add a default value for `JBH_JWT_SECRET` or `JBH_ADMIN_PASS`.

### Authorization

- Every admin handler (`Routes.ADMIN_*`) must call `authenticationOperationService.requireAdminRole()` first.
  The URL rule `/admin/**` in `AuthorizationRules` does not match the real path `/jbh-api/admin/**`. It protects nothing.
- User-group reads and changes must check owner or accepted member in the service
  (`requireOwner` / `requireMember`). Flag a lookup by group, invitation, or member id that skips it (IDOR).
- Everything under `/jbh-api/auth/**` is public. Flag a new endpoint there that reads or changes user data
  outside the sign-in, sign-up, refresh, and logout flows.
- An endpoint that returns another user's email or other personal data must be admin only.

### Horizontal scaling (N instances)

- The Caffeine user cache is per instance. An evict runs only on the instance that served the request.
  Flag new code whose security depends on an evict (role change, user delete, logout).
  Safe path: read the database for that decision.
- Uniqueness and one-time actions must be enforced by the database: a unique constraint, a conditional
  `UPDATE ... WHERE`, or optimistic locking. Flag check-then-insert on email, nickname, or invitation.
- `@Scheduled` jobs run on every instance. They must be idempotent, like `cleanupExpiredRefreshTokens`.
  A job that must run once needs a distributed lock (for example ShedLock).
- No HTTP session, no local files, no sticky routing. `SessionCreationPolicy.STATELESS` stays.
- Rate limits live in `jbh-gateway`. Do not add per-instance rate limits or counters in IAM.

### Database migrations (rolling deploy)

- Old and new instances run at the same time during a deploy. A changeset must work with the old code.
  Safe path: add columns as nullable or with a default; rename or drop in a later release.
- A new changeset needs a `rollback`.

### Contracts with other services

- `GET /jbh-api/users/find-user-id` is called by `jbh-personal-finance`. A change to its response is P1.
- A new top-level path under `/jbh-api` needs a route in `jbh-gateway`. Flag it if the pull request does not say so.
- No direct URL to another service. Use `jbh-gateway-client`.
- Port `9999`, Consul name `jbh-iam-service`, and database `jbh` are used by other repos.
  A change needs an update of the system map in `jbh-deploy`.
