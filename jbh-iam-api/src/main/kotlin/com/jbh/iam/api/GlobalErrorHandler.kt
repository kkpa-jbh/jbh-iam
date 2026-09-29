package com.jbh.iam.api

import com.jbh.iam.api.exceptions.ConstraintViolationsException
import com.jbh.iam.api.exceptions.web.BadRequestException
import com.jbh.iam.api.exceptions.web.ResourceNotFoundException
import com.jbh.iam.common.exceptions.BusinessException
import com.jbh.iam.common.exceptions.ForbiddenException
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

    @ExceptionHandler(ForbiddenException::class)
    fun forbiddenHandler(ex: ForbiddenException): ResponseEntity<ErrorMessageResponse> {
        log.warn("Access denied: ${ex.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorMessageResponse("You are not allowed to do this."))
    }

    // The Throwable handler below would turn these into 500, so their @ResponseStatus is applied here.
    @ExceptionHandler(ResourceNotFoundException::class)
    fun notFoundHandler(ex: ResourceNotFoundException): ResponseEntity<ErrorMessageResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorMessageResponse(ex.message ?: "Not found"))

    @ExceptionHandler(BadRequestException::class)
    fun badRequestHandler(ex: BadRequestException): ResponseEntity<ErrorMessageResponse> =
        ResponseEntity.badRequest().body(ErrorMessageResponse(ex.message ?: "Bad request"))

    @ExceptionHandler(Throwable::class)
    fun throwableHandler(ex: Throwable): ResponseEntity<ErrorMessageResponse> {
        log.error("An exception not caught: ${ex.message}", ex)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorMessageResponse("Working to fix this error. Be patience!"))
    }

}