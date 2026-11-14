## Context

JBH-74 section 4. The notification service lives in its own repo now. IAM must send the inviter's language
as `locale`. Facts checked in the code:

- IAM has no `@Async`, executor, or coroutine. The flow `UserGroupController.inviteUser` / `reinviteUser` ->
  `InvitationServiceImpl.inviteUserByEmailOrUsername` / `reinviteUserByEmailOrUsername` (`@Transactional`) ->
  `InvitationNotificationServiceImpl.sendInvitationNotification` runs on the HTTP request thread.
- `LocaleContextHolder.getLocale()` returns the JVM default locale when the header is missing. It cannot express "unset".
- `TeamInvitationMetadata.Builder.locale(String)` exists. A `null` locale is left out of the metadata map, so the
  notification service applies `withLocaleIfAbsent(...)` (default `es`).

## Goals / Non-Goals

**Goals:** the invitation email uses `en` or `es` from the inviter's `Accept-Language`; the package source list points to `jbh-notification`.

**Non-Goals:** other sections of JBH-74; the `sendAcceptInvitationNotification` TODO; any test code.

## Decisions

- **D1 - Language source: pass the header down (option A).** `UserGroupController.inviteUser` and `reinviteUser` read
  `@RequestHeader("Accept-Language", required = false) acceptLanguage: String?`. The value goes through
  `IInvitationService` / `InvitationServiceImpl` (both methods) to `IInvitationNotificationService.sendInvitationNotification`
  and its impl, as a new last parameter `acceptLanguage: String?`. It is explicit and keeps working if the send becomes async later.
  Rejected: B `LocaleContextHolder` (missing header gives the JVM default, not "unset"); C `RequestContextHolder`
  (ties the domain service to the servlet, breaks if async).
- **D2 - Multi-language header: best supported match by q-value.** Parse with `java.util.Locale.LanguageRange.parse(...)`
  (ranges come sorted by weight). Take the language part of each range and return the first one that is `en` or `es`.
  Examples: `fr-FR,es;q=0.9,en;q=0.8` -> `es`; `es-CO` -> `es`; `en-US,en;q=0.9` -> `en`; `fr-FR` -> `null`;
  `null` or blank -> `null`; malformed header -> `null` (catch `IllegalArgumentException`; the invite never fails because of the header).
  The wildcard `*` is not a supported language and is skipped.
- **D3 - SRP only.** New Kotlin `object InvitationLanguage` in `com.jbh.iam.domain.modules.usergroup.service` with one pure
  function `fromAcceptLanguage(header: String?): String?`. No interface (DIP rejected), no pluggable language list (OCP rejected).
- **D4 - No tests.** User decision: no unit and no integration tests; project rule "Do not generate any unit test code unless explicitly requested".
- **D5 - Scope limits.** No database change. No new `/jbh-api` path. No change to the JWT or cookies. The `sendAcceptInvitationNotification` TODO is untouched.
- **D6 - Package source (leader task).** Root `build.gradle.kts`, `PACKAGES_READ_TOKEN` block: `listOf("jbh-gateway-client", "jbh-notification")`.
  The comment says that the notification contracts now come from the `jbh-notification` repo.

## Responsibilities

- `UserGroupController.kt`: reads the `Accept-Language` header and passes it on. Changes when the HTTP contract changes.
- `IInvitationService.kt` / `InvitationServiceImpl.kt`: the invite flow; passes the header value through. Changes when invite rules change.
- `IInvitationNotificationService.kt` / `InvitationNotificationServiceImpl.kt`: builds and sends the invitation email;
  calls `InvitationLanguage` to get the locale. Changes when the email content changes.
- `InvitationLanguage.kt` (new): turns an `Accept-Language` value into `en`, `es`, or `null`. Changes when the supported languages change.
- Root `build.gradle.kts`: the package source list. Leader task.

## Concurrency

none. `InvitationLanguage` is a pure function with no shared state. The header value is a method argument, not thread-bound state.

## Database

No database change.

## Test contract

No tests are added (D4). Manual verification contract, for the user:

| Input (inviter's `Accept-Language`) | Expected `locale` in the request to the notification service |
|---|---|
| `en-US,en;q=0.9` (English browser) | `en` |
| `es-CO` (Spanish browser) | `es` |
| `fr-FR,es;q=0.9,en;q=0.8` | `es` |
| `fr-FR` | no `locale` key (service default `es`) |
| header missing or blank | no `locale` key |
| malformed value | no `locale` key, invite still returns 201 / 200 |

Check it with an invite and a reinvite from an English browser and from a Spanish browser, and read the notification request log line.

## Risks / Trade-offs

- Changing the signatures of `IInvitationService` and `IInvitationNotificationService` touches all callers. Callers found: only `UserGroupController` (2 calls) and `InvitationServiceImpl` (2 calls). There are no tests that call them.
- `sendInvitationNotification` logs the full request, which now includes the locale. It holds no secret.
