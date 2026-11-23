package com.jbh.iam.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Guard test for Kubernetes probes (JBH-86).
 *
 * The kubelet calls the liveness and readiness probes with no token and no cookie.
 * Spring Boot turns the probe groups on by itself when it runs on Kubernetes;
 * `spring.main.cloud-platform=kubernetes` makes the test see the same thing.
 * The test fails if a change in `AuthorizationRules` blocks the paths under `/actuator/health/`,
 * because then a pod would never become Ready.
 *
 * Real Postgres (Testcontainers), like [AuthFlowIntegrationTest]: the test is skipped when Docker is not running.
 */
@SpringBootTest(
    properties = [
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        // Base64 of 32 bytes. Only for this test.
        "app.jwtSecret=dGVzdC1zZWNyZXQtZm9yLXRoZS1pbnRlZ3JhdGlvbi10ZXN0cw==",
        "spring.main.cloud-platform=kubernetes",
    ]
)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class KubernetesProbesIntegrationTest {

    companion object {
        @Container
        @JvmField
        // The master changelog has a runningAs precondition: the DB user must be jbh_admin.
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply { withUsername("jbh_admin") }

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

    @Test
    fun `liveness probe answers UP without a token`() {
        mockMvc.perform(get("/actuator/health/liveness"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
    }

    @Test
    fun `readiness probe answers UP without a token`() {
        mockMvc.perform(get("/actuator/health/readiness"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
    }
}
