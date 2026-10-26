package com.jbh.iam.api

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Main flow against a real Postgres (Liquibase runs): sign up, sign in, refresh, reuse, log out.
 * The test is skipped when Docker is not running.
 */
@SpringBootTest(
    properties = [
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        // Base64 of 32 bytes. Only for this test.
        "app.jwtSecret=dGVzdC1zZWNyZXQtZm9yLXRoZS1pbnRlZ3JhdGlvbi10ZXN0cw=="
    ]
)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthFlowIntegrationTest {

    companion object {
        @Container
        @JvmField
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun databaseProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun postJson(path: String, body: Map<String, String>) =
        post(path).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body))

    private fun signIn(): Map<*, *> {
        val result = mockMvc.perform(
            postJson("/jbh-api/auth/signin", mapOf("usernameOrEmail" to "flow-user", "password" to "a-long-password"))
        ).andExpect(status().isOk).andReturn()
        return objectMapper.readValue(result.response.contentAsString, Map::class.java)
    }

    @Test
    fun `sign up, sign in, refresh, reuse of the old refresh token, log out`() {
        mockMvc.perform(
            postJson(
                "/jbh-api/auth/signup",
                mapOf(
                    "name" to "Flow User",
                    "username" to "flow-user",
                    "email" to "flow-user@example.com",
                    "password" to "a-long-password"
                )
            )
        ).andExpect(status().isCreated)

        val tokens = signIn()
        val accessToken = tokens["accessToken"] as String
        val oldRefreshToken = tokens["refreshToken"] as String

        val refreshed = mockMvc.perform(
            post("/jbh-api/auth/refresh").cookie(Cookie("JBH_REFRESH_TOKEN", oldRefreshToken))
        ).andExpect(status().isOk).andReturn()
        val newTokens = objectMapper.readValue(refreshed.response.contentAsString, Map::class.java)
        assertThat(newTokens["refreshToken"]).isNotEqualTo(oldRefreshToken)

        // Rotation: the old refresh token is revoked, so a second use must fail.
        mockMvc.perform(
            post("/jbh-api/auth/refresh").cookie(Cookie("JBH_REFRESH_TOKEN", oldRefreshToken))
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/jbh-api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .cookie(Cookie("JBH_REFRESH_TOKEN", newTokens["refreshToken"] as String))
        ).andExpect(status().isOk)

        // After logout the new refresh token is revoked too.
        mockMvc.perform(
            post("/jbh-api/auth/refresh").cookie(Cookie("JBH_REFRESH_TOKEN", newTokens["refreshToken"] as String))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `sign in with a wrong password is rejected`() {
        mockMvc.perform(
            postJson(
                "/jbh-api/auth/signup",
                mapOf(
                    "name" to "Wrong Pass",
                    "username" to "wrong-pass",
                    "email" to "wrong-pass@example.com",
                    "password" to "a-long-password"
                )
            )
        ).andExpect(status().isCreated)

        mockMvc.perform(
            postJson("/jbh-api/auth/signin", mapOf("usernameOrEmail" to "wrong-pass", "password" to "not-the-password"))
        ).andExpect(status().isUnauthorized)
    }
}
