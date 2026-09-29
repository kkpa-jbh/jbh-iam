package com.jbh.iam.common.exceptions

/**
 * The user is logged in but may not do this action. `GlobalErrorHandler` answers `403`.
 * Plain Kotlin on purpose: modules outside `jbh-iam-security` must not depend on Spring Security.
 */
class ForbiddenException(message: String) : RuntimeException(message)
