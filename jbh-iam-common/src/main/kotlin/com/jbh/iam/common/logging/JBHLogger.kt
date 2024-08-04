package com.jbh.iam.common.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

class JBHLogger(private val logger: Logger) {

    fun debug(message: String) {
        logger.debug(enrichMessage(message))
    }

    fun info(message: String) {
        logger.info(enrichMessage(message))
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            logger.error(enrichMessage(message), throwable)
        } else {
            logger.error(enrichMessage(message))
        }
    }

    private fun enrichMessage(message: String): String {
        val mdcData = MDC.getCopyOfContextMap() ?: emptyMap()
        return if (mdcData.isNotEmpty()) {
            "[${mdcData.entries.joinToString(", ") { "${it.key}=${it.value}" }}] $message"
        } else {
            message
        }
    }
}

inline fun <reified T : Any> T.customLogger(): JBHLogger = JBHLogger(LoggerFactory.getLogger(T::class.java))