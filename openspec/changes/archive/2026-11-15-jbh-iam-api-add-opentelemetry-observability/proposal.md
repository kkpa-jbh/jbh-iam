## Why

Plan JBH-55 (section 4, sub-issue JBH-58) gives the JBH system one trace per request, metrics, and logs in Loki. IAM sits behind `jbh-gateway`, which already starts the trace and sends `traceparent` (section 3). Today IAM has no tracing and no trace ids in logs. It also does not pass the trace on when it calls the notification service through `jbh-gateway-client`. Goals: G1, G2, G3, G4, G6.

## What Changes

- **Dependencies** (`jbh-iam-api/build.gradle.kts`): Micrometer tracing bridge to OpenTelemetry, OTLP exporter, OpenTelemetry Logback appender (pinned). `micrometer-registry-prometheus` is already there.
- **Trace**: IAM continues an incoming `traceparent` or starts a new trace. `sendNotification` continues the trace: the Spring `OpenTelemetry` bean goes into `JbhGatewayClientBuilder`.
- **Export gate**: `OTEL_SDK_DISABLED` (default `true`) turns only the sending of traces and OTLP logs on or off. Trace ids, log correlation and downstream `traceparent` work in both states. This differs from the plan text, as the user decided on 2026-11-15 (design D1).
- **Properties** (`application.properties`, `application-prod.properties`): OTLP endpoint (gRPC), sampling, resource attributes, `env` tag from `JBH_ENV`, shared latency bucket list for `http.server.requests`.
- **Logs**: `logback-spring.xml` keeps the text console and the `../logs/jbh-iam.log` file with `traceId`/`spanId` added. `prod` writes ECS JSON on stdout. The OpenTelemetry appender is on in every profile. New `OpenTelemetryAppenderConfig` installs it.
- **Docs**: "Observability" section in `.claude/CLAUDE.md`.

## Non-goals

- No unit tests (the plan excludes them). No test blueprint group.
- No database change.
- No change to `AuthorizationRules.kt` (`/actuator/prometheus` is already permitted) and no change to the actuator exposure (`health,info,prometheus`; the `local` profile keeps its existing `*`).
- No Spring Security dependency outside `jbh-iam-security`.
- No prod export (ticket JBH-84). The prod default stays export off.
- No change to `jbh-deploy`, to the `jbh-gateway-client` version, or to `../jbh-deploy/README.md` (no communication trigger is hit).
- No filter for Consul and Alloy scrape spans (accepted noise, design D13).

## Capabilities

### New Capabilities
- `iam-observability`: trace continuation and downstream propagation, export gate, Prometheus metrics, log correlation and OTLP logs, privacy rules for telemetry.

### Modified Capabilities
None.

## Impact

- New file: `jbh-iam-api/src/main/kotlin/com/jbh/iam/api/config/OpenTelemetryAppenderConfig.kt`.
- Changed files: `jbh-iam-api/build.gradle.kts`, `application.properties`, `application-prod.properties`, `logback-spring.xml`, `gateway/GatewayClientBuilder.kt`, `.claude/CLAUDE.md`.
- Existing test `AuthFlowIntegrationTest` loads the whole context (needs Docker). It must still start with export off and no collector.
