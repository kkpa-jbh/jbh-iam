package com.jbh.iam.viewgateway.web.service


import com.jbh.iam.common.config.JBHConstants.JBH_TOKEN_COOKIE_NAME
import com.jbh.iam.common.payload.LoginRequest
import com.jbh.iam.common.payload.SignUpRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.support.ServletUriComponentsBuilder


@Service
class ViewAuthService(private val restTemplate: RestTemplate) {

    private val AUTH_API_PATH = "/iam-api/auth";

    companion object {
        private val log = LoggerFactory.getLogger(ViewAuthService::class.java)
    }

    fun loginUser(loginRequest: LoginRequest): Pair<String, String> {
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val response = restTemplate.postForEntity(baseUrl + AUTH_API_PATH + "/signin", loginRequest, String::class.java)

        // Extract the Cookies from the response
        val jwtCookie = response.headers["Set-Cookie"]?.find { it.startsWith("$JBH_TOKEN_COOKIE_NAME=") }
        response.body?.let { log.info("Login response: $it") }

        return Pair("OK", jwtCookie ?: "")
    }

    fun registerUser(signUpRequest: SignUpRequest): String {
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val response =
            restTemplate.postForEntity("${baseUrl}/${AUTH_API_PATH}/signup", signUpRequest, String::class.java)
        return response.body ?: "Error occurred during registration"
    }

    fun checkUsernameAvailability(username: String): Boolean {
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val response =
            restTemplate.getForEntity(
                "${baseUrl}/${AUTH_API_PATH}/checkUsernameAvailability?nickName=$username",
                Map::class.java
            )
        return response.body?.get("available") as Boolean? ?: false
    }

    fun checkEmailAvailability(email: String): Boolean {
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        try {
            val response =
                restTemplate.getForEntity(
                    "${baseUrl}/${AUTH_API_PATH}/checkEmailAvailability?email=$email",
                    Map::class.java
                )
            return response.body?.get("available") as Boolean? ?: false
        } catch (e: Exception) {
            log.error("Error while checking email availability", e)
            e.printStackTrace()
        }
        return false
    }
}