package com.jbh.iam.common.exceptions

class BusinessException : Exception {
    val businessExceptionType: BusinessExceptionType

    constructor(businessExceptionType: BusinessExceptionType) : super(businessExceptionType.message) {
        this.businessExceptionType = businessExceptionType
    }

    constructor(businessExceptionType: BusinessExceptionType, vararg args: Any?) : super(businessExceptionType.getFormattedMessage(*args)) {
        this.businessExceptionType = businessExceptionType
    }

    companion object {
        // Generate serialVersionUID
        private const val serialVersionUID = 132234234234L
    }
}
