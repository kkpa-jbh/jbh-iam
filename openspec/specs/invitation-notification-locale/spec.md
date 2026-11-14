# invitation-notification-locale Specification

## Purpose
TBD - created by archiving change jbh-iam-api-invitation-locale-and-package-source. Update Purpose after archive.
## Requirements
### Requirement: Invitation email carries the inviter's language

When a user invites or reinvites someone, IAM SHALL read the optional `Accept-Language` header of that request.
IAM SHALL reduce it to the best supported language, `en` or `es`, and send it as `locale` in the
`TeamInvitationMetadata` of the notification request. When no supported language is found, IAM SHALL NOT set `locale`.

#### Scenario: English browser
- **WHEN** the inviter sends `Accept-Language: en-US,en;q=0.9`
- **THEN** the notification request has `locale` = `en`

#### Scenario: Spanish region
- **WHEN** the inviter sends `Accept-Language: es-CO`
- **THEN** the notification request has `locale` = `es`

#### Scenario: Several languages, best supported match
- **WHEN** the inviter sends `Accept-Language: fr-FR,es;q=0.9,en;q=0.8`
- **THEN** the notification request has `locale` = `es`

#### Scenario: Unsupported language
- **WHEN** the inviter sends `Accept-Language: fr-FR`
- **THEN** the notification request has no `locale` key

#### Scenario: Header missing or blank
- **WHEN** the request has no `Accept-Language` header, or the value is blank
- **THEN** the notification request has no `locale` key

#### Scenario: Malformed header
- **WHEN** the inviter sends an `Accept-Language` value that cannot be parsed
- **THEN** the notification request has no `locale` key
- **AND** the invitation still succeeds

#### Scenario: Reinvite
- **WHEN** the inviter calls the reinvite endpoint with `Accept-Language: en`
- **THEN** the notification request has `locale` = `en`

