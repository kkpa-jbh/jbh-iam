package com.jbh.iam.api

class Routes {

    companion object {
        const val IAM_BASE_PATH = "/iam-api"

        // Accounts
        const val ACCOUNT_PATH = "/accounts"
        const val TOTAL_ACCOUNTS = "/total"

        // Categories
        const val CATEGORIES_PATH = "/categories"
        const val USERS_PATH = "/users"
        const val GET_CATEGORY_BY_NAME_URI = "/{nickName}"

        // Authentication
        const val AUTH_PATH = "/auth"

        // Shopping
        const val USER_INFO_PATH = "/userinfo"
    }
}
