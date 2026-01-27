package com.jbh.iam.api.exceptions

import com.jbh.iam.common.exceptions.BusinessExceptionType

enum class IAMApiExceptionType (private val en: String, private val es: String) : BusinessExceptionType {
    GENERAL_ERROR("General error", "Error general"),
    
    USER_ALREADY_MEMBER("User %s is already a member of group %s", "El usuario %s ya es miembro del grupo %s"),
    USER_NOT_MEMBER("User %s is not a member of group %s", "El usuario %s no es miembro del grupo %s"),
    USER_NOT_ACCEPTED_MEMBER("User %s is not an accepted member of group %s and cannot invite others", "El usuario %s no es un miembro aceptado del grupo %s y no puede invitar a otros"),
    USER_ALREADY_HAS_MEMBERSHIP("User %s already has a membership record for group %s", "El usuario %s ya tiene un registro de membresía para el grupo %s"),
    NO_INVITATION_FOUND("No invitation found for user %s in group %s", "No se encontró invitación para el usuario %s en el grupo %s"),
    INVITATION_NOT_PENDING("Invitation is not pending. Current status: %s", "La invitación no está pendiente. Estado actual: %s")

    ;
    override fun getFormattedMessage(vararg args: Any?): String {
        return BusinessExceptionType.buildFormattedJsonMessage(en, es, args)
    }

    override val message: String
        get() = BusinessExceptionType.buildJsonMessage(en, es)
}