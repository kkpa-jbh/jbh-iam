package com.jbh.iam.domain.modules.usergroup.service

import java.util.Locale

object InvitationLanguage {

    private val SUPPORTED = setOf("en", "es")

    // Null means "no supported language": the notification service then applies its own default.
    fun fromAcceptLanguage(header: String?): String? {
        if (header.isNullOrBlank()) return null
        return try {
            Locale.LanguageRange.parse(header)
                .filter { it.weight > 0.0 }
                .map { it.range.substringBefore('-').lowercase() }
                .firstOrNull { it in SUPPORTED }
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
