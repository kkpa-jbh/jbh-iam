package com.jbh.iam.api.exceptions

import com.jbh.iam.common.payload.ErrorsResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ConstraintViolationsException(val errorsResponse: ErrorsResponse) : RuntimeException(errorsResponse.toString())