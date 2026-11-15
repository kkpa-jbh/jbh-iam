## Context

JBH-55 section 4 (JBH-58). IAM is Spring Boot 3.5.16 (servlet, Kotlin). Section 3 (`jbh-gateway`) is done and is the reference: same properties, same gate, same logback shape. Section 2 (`jbh-gateway-client`) is installed in `~/.m2` as `1.0.0-SNAPSHOT`; its builder has `openTelemetry(...)` (checked in the jar). No grill ran: the plan and the user decisions settle the design.

Facts checked:

- Settings live in `jbh-iam-api/src/main/resources`: `application.yml` (server, app, spring) and `application.properties` (actuator exposure, `management.metrics.tags.service=jbh-iam`, `management.metrics.tags.env=dev`, gateway URL). Profile files: `application-local.properties` (exposure `*`, DEBUG levels), `application-prod.properties` (exposure `health,info,prometheus`, `env=prod`), `application-test.properties`. The default active profile is `local` (set in `application.yml`). `application.properties` wins over `application.yml` for the same key. New telemetry settings go next to the existing `management.*` settings in `application.properties` and `application-prod.properties`.
- `spring.application.name` is `jbh-iam-service` (the Consul name). Boot uses it as `service.name`. The metric tag `service=jbh-iam` stays as it is.
- `GatewayClientBuilder` is a `@Configuration` class with a constructor (`configProvider`, `meterRegistry`). `GatewayClientFactoryImpl` calls `buildGatewayClient()`. No test builds `GatewayClientBuilder` by hand (searched `src/test`). Only `AuthFlowIntegrationTest` (Docker, skipped without Docker) loads the Spring context.
- `AuthorizationRules.kt` already permits `/actuator/prometheus`. Not touched.

### Library docs (Context7 CLI, Spring Boot 3.5 docs; plus the jar checks done for section 3)

| Topic | Result | Differs from plan? |
|---|---|---|
| OTLP properties | `management.otlp.tracing.{endpoint,transport,export.enabled}` and `management.otlp.logging.{endpoint,transport,export.enabled}` | No |
| Transport | Boot default is `http` (HTTP/protobuf). gRPC needs `management.otlp.tracing.transport=grpc` (same for logging). Alloy listens on gRPC (`14317` / `jbh-alloy:4317`), so we set `grpc`. A gRPC endpoint has no path suffix. | Plan said "check": now fixed to `grpc` |
| Sampling | `management.tracing.sampling.probability`, Boot default `0.1`, so we set `${OTEL_TRACES_SAMPLER_ARG:1.0}` | No |
| Propagation | `management.tracing.propagation.produce` default `[W3C]`, `consume` default `[W3C, B3, B3_MULTI]`. So the `OpenTelemetry` bean sends W3C `traceparent`. No setting needed. | Confirms plan |
| Resource attributes | `management.opentelemetry.resource-attributes` (a map; dotted keys need brackets). `service.name` comes from `spring.application.name`. | No |
| Histogram | `management.metrics.distribution.slo[http.server.requests]`. Micrometer parses `ms` as whole numbers, so `2.5s` is written `2500ms`. | Plan wrote `1s`, `2.5s`, `5s`, `10s`: written as ms |
| Structured logs | Boot has `logging.structured.format.console=ecs`. With a custom `logback-spring.xml` use `org.springframework.boot.logging.logback.StructuredLogEncoder` with `<format>ecs</format>`. | No |
| OTel appender | Boot docs: declare `OpenTelemetryAppender` in `logback-spring.xml`, and install it with an `InitializingBean` that calls `OpenTelemetryAppender.install(openTelemetry)`. | No |
| Export flag | `export.enabled` is true when missing, so we must set it. | Gate is export-only (user decision) |

## Decisions

- **D1: `OTEL_SDK_DISABLED` controls only the export.** `management.otlp.tracing.export.enabled` and `management.otlp.logging.export.enabled` are `true` only when `OTEL_SDK_DISABLED=false`. `management.tracing.enabled` is not set (default `true`). So with export off IAM still has trace ids, `traceId` in logs and `traceparent` on the notification call. Default is export off, so prod behaves as today until JBH-84. User decision 2026-11-15; it replaces the plan's earlier "map it to `management.tracing.enabled`".
- **D2: Invert the flag with a lookup table, as in `jbh-gateway`.** Placeholders cannot negate. In `application.properties`:

  ```properties
  jbh.otel.export-enabled-when-sdk-disabled.true=false
  jbh.otel.export-enabled-when-sdk-disabled.false=true
  jbh.otel.export-enabled=${jbh.otel.export-enabled-when-sdk-disabled.${OTEL_SDK_DISABLED:true}:false}
  management.otlp.tracing.export.enabled=${jbh.otel.export-enabled}
  management.otlp.logging.export.enabled=${jbh.otel.export-enabled}
  ```

  Unset gives `true`, so export is off. `false` gives export on. Any other value (`banana`, empty, `FALSE`) finds no key and falls back to `false`, so export is off. No value can fail the start. Rejected: an `EnvironmentPostProcessor` (a new class and a `spring.factories` entry for one boolean), SpEL (not in property files).
- **D3: gRPC, one endpoint.** `management.otlp.tracing.endpoint` and `management.otlp.logging.endpoint` are `${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:14317}`; both `transport=grpc`. Docker sets `http://jbh-alloy:4317`.
- **D4: Sampling and propagation.** `management.tracing.sampling.probability=${OTEL_TRACES_SAMPLER_ARG:1.0}` (parent-based in Boot). W3C is the Boot default. `management.tracing.baggage.enabled=false`: IAM does not need baggage, and the gateway already drops it from internet requests.
- **D5: Resource attributes** in `application.properties` (bracket keys, because names hold dots):

  ```properties
  management.opentelemetry.resource-attributes[service.namespace]=jbh
  management.opentelemetry.resource-attributes[deployment.environment.name]=${JBH_ENV:dev}
  management.opentelemetry.resource-attributes[service.version]=${JBH_SERVICE_VERSION:unknown}
  ```

  `application-prod.properties` overrides the environment default to `${JBH_ENV:prod}`. Today's `dev` / `prod` defaults stay. Local runs set `JBH_ENV=local`. `service.name` is `jbh-iam-service` from `spring.application.name`. `logging.structured.ecs.service.environment` and `.version` get the same values in `application-prod.properties` (the JSON log is used only in `prod`; the version also goes in the base file so a local `prod` run is complete).
- **D6: Metrics.** `micrometer-registry-prometheus` is already a `runtimeOnly` dependency; no change. `management.metrics.tags.env=${JBH_ENV:dev}` (prod file: `${JBH_ENV:prod}`); `management.metrics.tags.service=jbh-iam` unchanged. Histogram:

  ```properties
  management.metrics.distribution.slo[http.server.requests]=5ms,10ms,25ms,50ms,100ms,250ms,500ms,1000ms,2500ms,5000ms,10000ms
  ```

  With `slo` the registry publishes these 11 buckets plus `+Inf`. We do not set `percentiles-histogram` (about 70 more buckets). Exposure stays: `health,info,prometheus` in the base and `prod` files; the `local` profile keeps its existing `*` (it is behind login; not changed here).
- **D7: `logback-spring.xml`, one job: log format per profile.**
  - `!prod`: text console (existing pattern, tidied into one line) and the `FILE` appender `../logs/jbh-iam.log` (same rolling policy). Both patterns get `[%X{traceId:-},%X{spanId:-}]`. The keys are the MDC names that Micrometer Tracing writes; the pattern is explicit, so it does not depend on a Boot default property.
  - `prod`: console appender with `StructuredLogEncoder`, `<format>ecs</format>`. The ECS writer adds the trace id and span id from the MDC. No file, because the container file system is read-only (existing rule).
  - `OpenTelemetryAppender` (`io.opentelemetry.instrumentation.logback.appender.v1_0`) on `root` in every profile. Only default captured attributes (no MDC, no arguments, no code attributes).
  - **No feedback loop.** The loggers `io.opentelemetry`, `io.grpc`, `okhttp3`, `okio` are `additivity="false"` and write to the console only. Exporter errors never reach the OTel appender.
  - **Missing collector.** Start: the gRPC channel is lazy, nothing blocks. Run time: the SDK exports in a batch processor with a bounded queue (2048); when full it drops records and never blocks a request thread. The SDK throttles its own warnings. Together with the exclusion above, the log shows a few lines per minute at most, not a flood. With export off, no exporter exists and nothing leaves the process.
- **D8: `OpenTelemetryAppenderConfig` (one job: install the appender).** A `@Component` in `com.jbh.iam.api.config`, `InitializingBean`, calls `OpenTelemetryAppender.install(openTelemetry)` with the Spring `OpenTelemetry` bean (as the Boot docs show). The appender buffers early events until then and replays them. A second `install` (several test contexts in one JVM) is safe.
- **D9: Notification calls continue the trace.** `GatewayClientBuilder` gets a third constructor parameter `openTelemetry: io.opentelemetry.api.OpenTelemetry` and calls `.openTelemetry(openTelemetry)` on `JbhGatewayClientBuilder`. `GlobalOpenTelemetry` is never read. The Spring bean has the W3C propagator (D-docs table), so `traceparent` goes out. The span name is `<METHOD> <url.template>` and holds no id, query, header or body (library README, "Tracing"). The library version stays `1.0.0-SNAPSHOT`.
- **D10: Dependencies** in `jbh-iam-api/build.gradle.kts`: `io.micrometer:micrometer-tracing-bridge-otel` and `io.opentelemetry:opentelemetry-exporter-otlp` (Boot BOM versions), and `io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.15.0-alpha` (pinned; the BOM does not manage it; `2.15.0-alpha` is the line built for OpenTelemetry SDK 1.49.0, the version the Boot 3.5.16 BOM manages, same pin as `jbh-gateway`). The implementer confirms it in the resolved POM and does not pick a line that needs a newer SDK. All three as `implementation`. `micrometer-registry-prometheus` unchanged.
- **D11: Security.**
  - `AuthorizationRules.kt` is not changed. `/actuator/prometheus` stays as permitted today. Spring Security stays only in `jbh-iam-security`; no dependency added there.
  - Server span attribute `http.url` is the path only (Spring MVC default convention), so a query string (for example an email in `checkEmailAvailability`) is not in the span. The client span has the template only. Metric `uri` tags are templates (`UNKNOWN`/`NOT_FOUND` otherwise). No new log statement logs a body, cookie, `Authorization` or token. Do not enable `spring.mvc.log-request-details` or TRACE for `org.springframework.web` (header values would appear). The existing `insights.web.log-request-details=false` line stays.
  - The `local` profile's `logging.level.com.jbh.gateway.internal=DEBUG` is not changed. The user checks M9 below.
- **D12: Series.** One timer with SLO buckets is 15 series per tag combination. IAM has about 25 endpoints, so the worst case is below 1,000 series for `http_server_requests`, about 1,300 in total. Budget for IAM: at most 1,500. User measures with `curl -s localhost:9999/actuator/prometheus | grep -vc '^#'`. Over budget: drop the histogram first.
- **D13: Accepted noise.** Alloy (scrape every 30 s) and Consul (health check every 60 s) make a server span each, about 3 per minute. Excluding them needs an `ObservationPredicate` class. Not done in this change.

## Responsibilities

- `jbh-iam-api/src/main/kotlin/com/jbh/iam/api/config/OpenTelemetryAppenderConfig.kt` (new): installs the OpenTelemetry Logback appender with the Spring `OpenTelemetry` bean at start. Changes when the log export wiring changes.
- `GatewayClientBuilder.kt` (existing): builds the gateway client. New input: the `OpenTelemetry` bean. Same job as before.
- `logback-spring.xml` (existing): log format per profile and appender wiring.
- `application.properties`, `application-prod.properties`, `build.gradle.kts` (existing): config and libraries only. No code.
- No new interface and no abstraction.

## Database

No database change.

## Concurrency

none. Reasons:

- `OpenTelemetryAppenderConfig`: `install` runs once at bean start. The library buffers and replays early events and is thread-safe. A second `install` is harmless.
- `GatewayClientBuilder`: reads one injected bean; no shared mutable state added. The client is created once by `GatewayClientFactoryImpl` (as today).
- Export is asynchronous inside the OpenTelemetry SDK (library code, bounded queue). We add no thread, no shared cache, no check-then-act.

## Test contract

- **Test blueprint: does not apply.** The plan excludes unit tests, and this change has no unit-testable logic (properties, one XML file, one `InitializingBean`, one extra constructor argument). tasks.md has no blueprint group. An existing test changes only if this change breaks it; none is expected.
- **Automated checks (leader):** `./gradlew build` and `./gradlew :jbh-iam-api:test`. `AuthFlowIntegrationTest` (needs Docker) loads the full context with export off and no collector. The new placeholders, the `OpenTelemetry` bean and the appender must wire there.
- **Manual checks (user).** Start IAM with `./gradlew bootRun` (needs `JBH_JWT_SECRET`, `JBH_ADMIN_PASS`, Postgres). `OTEL_SDK_DISABLED=false`, `JBH_ENV=local` unless stated. Alloy and LGTM run as in section 1.

  | # | Input | Expected result |
  |---|---|---|
  | M1 | `curl -s localhost:9999/actuator/prometheus` | `200`. `http_server_requests_seconds_bucket` has exactly the `le` values `0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0, +Inf`. Labels `service="jbh-iam"` and `env="local"`. |
  | M2 | `curl -i -H 'traceparent: 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01' 'localhost:9999/jbh-api/auth/checkEmailAvailability?email=x@y.z'` | Log line of the request has `[0af7651916cd43dd8448eb211c80319c,<16 hex>]`. In Tempo the trace `0af7651916cd43dd8448eb211c80319c` has an IAM server span with `service.name=jbh-iam-service`, `service.namespace=jbh`, `deployment.environment.name=local`. `grep -c 'x@y.z'` on the metrics output prints `0`, and no span attribute holds `x@y.z`. |
  | M3 | Same request without `traceparent` | Log line has a 32-hex `traceId` and a 16-hex `spanId`. |
  | M4 | Start with `OTEL_SDK_DISABLED` unset, then M3 | IAM starts. `traceId` is in the log. No connection to a collector is tried. |
  | M5 | Start with `OTEL_SDK_DISABLED=banana` | IAM starts. Export is off (same as M4). |
  | M6 | Start with `OTEL_SDK_DISABLED=false`, no collector | IAM starts. `/actuator/health` is `UP`. At most a few exporter warnings per minute, and none is repeated as an OTLP log. Requests are not slowed. |
  | M7 | Through the gateway, invite a user to a group (sends the notification) | One trace: gateway, IAM server span, a CLIENT span named `<METHOD> <url.template>` in IAM, then the gateway and `jbh-notification`. Same trace id. |
  | M8 | Grafana, Loki `{service_name="jbh-iam-service"} \|= "<trace id from M2>"` | At least one line with `traceId` and `spanId`. |
  | M9 | Sign in (`POST /jbh-api/auth/signin`) and read the trace and the log file | No password, JWT, refresh token or cookie in any span attribute or log line. |
  | M10 | `SPRING_PROFILES_ACTIVE=prod`, then M3 | stdout is one ECS JSON object per line with the trace id and span id. The same lines reach Loki. `env` tag is `prod` unless `JBH_ENV` is set. |
  | M11 | `curl -i localhost:9999/actuator/prometheus` with no token (profile `prod`) | `200`. `git diff` shows `AuthorizationRules.kt` unchanged. |
  | M12 | Series count: `curl -s localhost:9999/actuator/prometheus \| grep -vc '^#'` | At most about 1,500 (D12). Report the number for the plan's budget table. |

- **Integration tests:** none added.

## Risks / Trade-offs

- The `OpenTelemetry` bean must carry the W3C propagator. Boot docs say the default is W3C; M7 proves it (no `traceparent` at the gateway would show as a new trace).
- The Logback appender is an alpha artifact. It is pinned, so a Boot upgrade does not move it silently.
- `management.tracing.sampling.probability` below 1.0 later means an unsampled trace id has no trace in Tempo. Acceptable.
- `GraalVM nativeCompile` is not checked.
