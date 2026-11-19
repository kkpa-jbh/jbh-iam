## ADDED Requirements

### Requirement: IAM continues or starts a trace

IAM SHALL continue the trace of an incoming W3C `traceparent` header. When none comes in, IAM SHALL start a new trace. Sampling SHALL be parent-based with the ratio from `OTEL_TRACES_SAMPLER_ARG` (default `1.0`).

#### Scenario: Incoming traceparent
- **WHEN** a request carries `traceparent: 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01`
- **THEN** every log line of that request has `traceId` `0af7651916cd43dd8448eb211c80319c`

#### Scenario: No traceparent
- **WHEN** a request carries no `traceparent`
- **THEN** its log lines have a non-empty 32-character `traceId` and a non-empty `spanId`

### Requirement: Export gate

`OTEL_SDK_DISABLED` SHALL control only the OTLP export of traces and logs. Tracing SHALL stay on in every profile. Export SHALL be on only when `OTEL_SDK_DISABLED` is exactly `false`.

#### Scenario: Default
- **WHEN** `OTEL_SDK_DISABLED` is unset
- **THEN** IAM starts, makes no connection to a collector, and log lines still carry `traceId`

#### Scenario: Export on
- **WHEN** `OTEL_SDK_DISABLED=false` and a collector listens on `OTEL_EXPORTER_OTLP_ENDPOINT`
- **THEN** spans and log records reach the collector with resource attributes `service.name=jbh-iam-service`, `service.namespace=jbh`, `deployment.environment.name` from `JBH_ENV`, `service.version` from `JBH_SERVICE_VERSION`

#### Scenario: Other value
- **WHEN** `OTEL_SDK_DISABLED=banana`
- **THEN** IAM starts and export is off

#### Scenario: Collector missing
- **WHEN** `OTEL_SDK_DISABLED=false` and no collector runs
- **THEN** IAM starts, `/actuator/health` is `UP`, requests are not slowed, and the log shows no flood of exporter errors

### Requirement: Notification calls continue the trace

`GatewayClientBuilder` SHALL pass the Spring `OpenTelemetry` bean to `JbhGatewayClientBuilder`, so `sendNotification` makes a client span and sends `traceparent` to `jbh-gateway`.

#### Scenario: Invitation email
- **WHEN** a user invites someone and IAM calls `sendNotification`
- **THEN** the trace of the invite request holds a CLIENT span for that call, and the request to the gateway has the same trace id

### Requirement: Metrics

`/actuator/prometheus` SHALL publish `http.server.requests` with exactly the buckets 5ms, 10ms, 25ms, 50ms, 100ms, 250ms, 500ms, 1s, 2.5s, 5s, 10s plus `+Inf`. Every metric SHALL carry `service` and `env` (`env` from `JBH_ENV`, default `dev`, `prod` in the `prod` profile). Exposure SHALL stay `health,info,prometheus`. No label SHALL hold a user id, email, IP or raw path.

#### Scenario: Buckets
- **WHEN** the endpoint is read after some requests
- **THEN** `http_server_requests_seconds_bucket` has `le` values `0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0, +Inf`

#### Scenario: Endpoint stays public, rules unchanged
- **WHEN** `GET /actuator/prometheus` is called without a token
- **THEN** the answer is `200`, and `AuthorizationRules.kt` is not changed

### Requirement: Logs

Every log line SHALL carry `traceId` and `spanId`. The `prod` profile SHALL write ECS JSON on stdout. Other profiles SHALL keep the text console and `../logs/jbh-iam.log`. A log record SHALL go to the OTLP exporter in every profile. Exporter errors SHALL NOT go to the OTLP exporter.

#### Scenario: Local text log
- **WHEN** IAM runs with the `local` profile and handles a request
- **THEN** console and file lines contain `[<traceId>,<spanId>]`

#### Scenario: Prod JSON log
- **WHEN** IAM runs with `SPRING_PROFILES_ACTIVE=prod`
- **THEN** each stdout line is one ECS JSON object that holds the trace id and span id

### Requirement: No secrets in telemetry

No password, JWT, refresh token, Google ID token, cookie or `Authorization` header SHALL appear in a log line, a span attribute or a metric label.

#### Scenario: Sign in
- **WHEN** a user signs in and the trace is read in Tempo
- **THEN** no span attribute holds the password, a token or a cookie, and the server span `http.url` has the path only
