package com.jbh.iam.api

/**
 *  API Routes: /jbh-api/{domain}/
 *   Authentication & Users:
 *   ├── /jbh-api/auth/signin
 *   ├── /jbh-api/auth/signup
 *   ├── /jbh-api/auth/userinfo
 *   ├── /jbh-api/auth/check-username
 *   └── /jbh-api/users/{id}
 */
class Routes {

    companion object {
        const val IAM_BASE_API_PATH = "/jbh-api"

        // Categories
        const val USERS_PATH = "/users"

        // Authentication
        const val AUTH_PATH = "/auth"

        // Logout
        const val LOGOUT_PATH = "/logout"

        const val USER_INFO_PATH = "/userinfo"
    }
}
