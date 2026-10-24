package com.jbh.iam.common.exceptions

/**
 * Sign-in failed: wrong user or wrong password. `GlobalErrorHandler` answers `401`.
 * The message is generic on purpose: it must not tell whether the user exists.
 * Plain Kotlin on purpose: modules outside `jbh-iam-security` must not depend on Spring Security.
 */
class InvalidCredentialsException : RuntimeException("Invalid username or password")
