package com.jbh.iam.api

import com.jbh.iam.api.exceptions.ConstraintViolationsException
import com.jbh.iam.common.exceptions.BusinessException
import com.jbh.iam.common.payload.ErrorMessageResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalErrorHandler {

    private val log = LoggerFactory.getLogger(GlobalErrorHandler::class.java)

    @ExceptionHandler(ConstraintViolationsException::class)
    @ResponseBody
    fun constraintViolationsHandler(
        ex: ConstraintViolationsException,
        request: WebRequest
    ): ResponseEntity<Any> {

        val messageResponse = ex.errorsResponse.errors.map {
            it.message
        }.joinToString(separator = " - ")

        return ResponseEntity.badRequest().body(ErrorMessageResponse(messageResponse))
    }

    @ExceptionHandler(BusinessException::class)
    @ResponseBody
    fun businessExceptionHandler(
        ex: BusinessException,
        request: WebRequest
    ): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorMessageResponse(ex.message ?: ex.businessExceptionType.message))
    }

    @ExceptionHandler(Throwable::class)
    fun throwableHandler(ex: Throwable): ResponseEntity<ErrorMessageResponse> {
        ex.printStackTrace();
        log.error("An exception not caught: ${ex.message} ")

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorMessageResponse("Working to fix this error. Be patience!"))
    }

}