package com.jbh.iam.common.exceptions

import java.util.*


enum class CommonExceptionType(private val en: String, private val es: String) : BusinessExceptionType {
    INVALID_PERCENTAGE("Percentage must be between 0 and 100", "Porcentaje debe estar entre 0 y 100");

    override fun getFormattedMessage(vararg args: Any?): String {
        return BusinessExceptionType.buildFormattedJsonMessage(en, es, args)
    }

    override val message: String
        get() = BusinessExceptionType.buildJsonMessage(en, es)
}
