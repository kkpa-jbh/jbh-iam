package com.jbh.iam.common.payload

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SignUpRequestTest {

    @Test
    fun `toString never prints the password`() {
        val request = SignUpRequest(name = "Ana", username = "ana", email = "ana@example.com", password = "s3cret-pass")

        assertThat(request.toString())
            .doesNotContain("s3cret-pass")
            .contains("password=***")
    }
}
