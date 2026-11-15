## 1. Language parser (module `jbh-iam-api`)

- [x] 1.1 Create `jbh-iam-api/src/main/kotlin/com/jbh/iam/domain/modules/usergroup/service/InvitationLanguage.kt`: `object InvitationLanguage` with `fun fromAcceptLanguage(header: String?): String?` (D2, D3). Depends on: none.

## 2. Pass the header down (module `jbh-iam-api`)

- [x] 2.1 `IInvitationNotificationService.kt` and `InvitationNotificationServiceImpl.kt`: add last parameter `acceptLanguage: String?` to `sendInvitationNotification`; set `.locale(InvitationLanguage.fromAcceptLanguage(acceptLanguage))` on `TeamInvitationMetadata.builder()` (D1). Do not touch `sendAcceptInvitationNotification`. Depends on: 1.1.
- [x] 2.2 `IInvitationService.kt` and `InvitationServiceImpl.kt`: add last parameter `acceptLanguage: String?` to `inviteUserByEmailOrUsername` and `reinviteUserByEmailOrUsername`; pass it to `sendInvitationNotification` (D1). Depends on: 2.1.
- [x] 2.3 `UserGroupController.kt`: add `@RequestHeader("Accept-Language", required = false) acceptLanguage: String?` to `inviteUser` and `reinviteUser`; pass it to the service (D1). Depends on: 2.2.

## 3. Package source (owner: leader)

- [ ] 3.1 Root `build.gradle.kts` (outside module `jbh-iam-api`): change `listOf("jbh-gateway-client", "jbh-personal-finance")` to `listOf("jbh-gateway-client", "jbh-notification")` and update the comment: the notification contracts now come from the `jbh-notification` repo (D6). Depends on: none.

## 4. Docs (module `jbh-iam-api` side)

- [ ] 4.1 `.claude/CLAUDE.md` line "The notifications module lives in `jbh-personal-finance`" is out of date after JBH-74. Leader decides whether to fix it in this change. Depends on: none.

Tasks 1.1 to 2.3 are one chain (same few files, one worker). Task 3.1 is independent of them.

No test blueprint group: this change has no unit or integration tests (D4).

Size estimate: 7 files, ~70 changed lines (tests and docs included)
