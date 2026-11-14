## Why

JBH-74 moves the notification service out of `jbh-personal-finance` into its own repo, `jbh-notification`.
Two things change for `jbh-iam`:

- The notification contracts package now comes from the `jbh-notification` repo on GitHub Packages.
- An invitation email must use the language of the inviter. The notification service already accepts a
  `locale` in `TeamInvitationMetadata`. IAM does not send it today, so every email uses the default (`es`).

## What Changes

- Root `build.gradle.kts` (leader task): the `PACKAGES_READ_TOKEN` repo list becomes
  `listOf("jbh-gateway-client", "jbh-notification")`. The comment says the notification contracts now come
  from the `jbh-notification` repo.
- `UserGroupController.inviteUser` and `reinviteUser` read the optional `Accept-Language` header and pass it
  down through `IInvitationService` / `InvitationServiceImpl` to `IInvitationNotificationService.sendInvitationNotification`.
- New `InvitationLanguage` object turns an `Accept-Language` value into `en`, `es`, or `null`.
- `InvitationNotificationServiceImpl.sendInvitationNotification` sets `.locale(...)` on
  `TeamInvitationMetadata.builder()`. A `null` value leaves the locale unset, and the notification service uses its default.

## Capabilities

### New Capabilities

- `invitation-notification-locale`: the invitation email carries the inviter's language (`en` or `es`) when known.

### Modified Capabilities

None.

## Impact

- Module `jbh-iam-api`: `UserGroupController.kt`, `IInvitationService.kt`, `InvitationServiceImpl.kt`,
  `IInvitationNotificationService.kt`, `InvitationNotificationServiceImpl.kt`, new `InvitationLanguage.kt`.
- Root `build.gradle.kts` (outside the module, leader task).
- HTTP contract: the invite and reinvite endpoints accept an optional `Accept-Language` request header.
  No new path, no change to the response, the JWT, or the cookies.
- No database change.

## Not included

- The `jbh-gateway-client` and `jbh-notification` changes (other sections of JBH-74).
- The `sendAcceptInvitationNotification` TODO stays as it is.
- The `jbh-deploy` system map: this change adds and removes no service-to-service call, and the calls
  already go through `jbh-gateway-client`. No README sync is needed.
- No unit tests and no integration tests (user decision).
