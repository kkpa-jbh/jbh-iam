package com.jbh.iam.common.exceptions

import java.util.*

interface BusinessExceptionType {
    fun getFormattedMessage(vararg args: Any?): String {
        return String.format(Locale.ROOT, this.message, *args)
    }

    val message: String

    companion object {
        fun buildJsonMessage(en: String, es: String): String {
            return """
           {
             "en": "${escapeJson(en)}",
             "es": "${escapeJson(es)}"
           }
           """.trimIndent()
        }

        fun buildFormattedJsonMessage(en: String, es: String, args: Array<out Any?>): String {
            val formattedEn = if (args.isNotEmpty()) {
                String.format(Locale.ROOT, en, *args)
            } else {
                en
            }
            
            val formattedEs = if (args.isNotEmpty()) {
                String.format(Locale.ROOT, es, *args)
            } else {
                es
            }
            
            return """
           {
             "en": "${escapeJson(formattedEn)}",
             "es": "${escapeJson(formattedEs)}"
           }
           """.trimIndent()
        }

        private fun escapeJson(value: String): String {
            // Basic escaping for quotes, backslashes, newlines, and other special characters
            return value.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t")
        }
    }
}
