package com.jbh.iam.api

import com.jbh.iam.api.exceptions.web.BadRequestException
import com.jbh.iam.api.exceptions.web.ResourceNotFoundException
import com.jbh.iam.common.exceptions.ForbiddenException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalErrorHandlerTest {

    private val handler = GlobalErrorHandler()

    @Test
    fun `forbidden gives 403 and hides the internal message`() {
        val response = handler.forbiddenHandler(ForbiddenException("User X is not the owner of group Y"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        assertThat(response.body!!.message).doesNotContain("owner of group")
    }

    @Test
    fun `not found and bad request keep their status`() {
        assertThat(handler.notFoundHandler(ResourceNotFoundException("User", "nickname", "x")).statusCode)
            .isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(handler.badRequestHandler(BadRequestException("bad")).statusCode)
            .isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `unknown errors give 500`() {
        assertThat(handler.throwableHandler(IllegalStateException("boom")).statusCode)
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
