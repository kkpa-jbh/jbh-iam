## 1. Build and wiring (module `jbh-iam-api`)

- [ ] 1.1 `jbh-iam-api/build.gradle.kts`: add `implementation("io.micrometer:micrometer-tracing-bridge-otel")`, `implementation("io.opentelemetry:opentelemetry-exporter-otlp")` and `implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.15.0-alpha")` with a short comment on the pin (D10). Keep `micrometer-registry-prometheus`. Depends on: none.
- [ ] 1.2 `jbh-iam-api/src/main/kotlin/com/jbh/iam/api/config/OpenTelemetryAppenderConfig.kt` (new): `@Component class OpenTelemetryAppenderConfig(private val openTelemetry: OpenTelemetry) : InitializingBean`; `afterPropertiesSet()` calls `OpenTelemetryAppender.install(openTelemetry)` (D8). Depends on: 1.1.
- [ ] 1.3 `jbh-iam-api/src/main/kotlin/com/jbh/iam/gateway/GatewayClientBuilder.kt`: add constructor parameter `openTelemetry: OpenTelemetry` and `.openTelemetry(openTelemetry)` in `buildGatewayClient()`; short comment that the bean makes `sendNotification` continue the trace (D9). Depends on: 1.1.

## 2. Properties (module `jbh-iam-api`)

- [ ] 2.1 `application.properties`: add the `jbh.otel` lookup table, OTLP tracing and logging (endpoint, `transport=grpc`, `export.enabled`), sampling, baggage off, resource attributes, ECS service version, SLO buckets; set `management.metrics.tags.env=${JBH_ENV:dev}`. Keep exposure and `service` tag as they are (D1 to D6). Depends on: none.
- [ ] 2.2 `application-prod.properties`: `management.metrics.tags.env=${JBH_ENV:prod}`, resource attribute `deployment.environment.name=${JBH_ENV:prod}`, `logging.structured.ecs.service.environment=${JBH_ENV:prod}`. Exposure stays `health,info,prometheus` (D5, D6). Depends on: none.

## 3. Logs (module `jbh-iam-api`)

- [ ] 3.1 `logback-spring.xml`: `!prod` text console and file, both with `[%X{traceId:-},%X{spanId:-}]`; `prod` ECS JSON console; `OTEL` appender on root in every profile; `io.opentelemetry`, `io.grpc`, `okhttp3`, `okio` loggers with `additivity="false"` to the console only (D7). Depends on: 1.1 (the appender class must be on the classpath), 1.2 (installs it).

## 4. Docs

- [ ] 4.1 `.claude/CLAUDE.md`: new "Observability" section: signals (traces, metrics, logs), env vars (`OTEL_SDK_DISABLED`, `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_TRACES_SAMPLER_ARG`, `JBH_ENV`, `JBH_SERVICE_VERSION`) with defaults, the export-only gate, local check (IDE: `OTEL_SDK_DISABLED=false`, `JBH_ENV=local`, endpoint default `http://localhost:14317`; Docker: `http://jbh-alloy:4317`), the no-secrets rule. Add the new env vars to the "Key environment variables" list. `dev-docs/` holds no run notes, so it is not changed. Depends on: none.

Tasks 1.1 to 1.3 and 3.1 touch different files. 1.2, 1.3 and 3.1 need 1.1 only to compile. Tasks 2.1, 2.2 and 4.1 have no dependency. One worker can do all of it in one pass.

No test blueprint group: the plan excludes unit tests, and this change has none (design: Test contract).

Size estimate: 7 files, ~190 changed lines (tests and docs included)
