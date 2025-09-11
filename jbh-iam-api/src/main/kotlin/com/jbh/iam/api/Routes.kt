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
        const val REQ_SOURCE_HEADER = "JBH-XXX-REQ-SOURCE"


        // Authentication
        const val AUTH_PATH = "/auth"

        // Logout
        const val LOGOUT_PATH = "/logout"

        // Users
        const val USERS_PATH = "/users"
        const val USER_INFO_PATH = "/userinfo"
        const val FIND_USERID_PATH = "/find-user-id"
    }
}
