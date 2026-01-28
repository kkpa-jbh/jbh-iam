package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.common.exceptions.BusinessExceptionType
import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.domain.modules.user.model.InternalUserDTO
import com.jbh.iam.domain.modules.usergroup.model.InvitationRequest
import com.jbh.iam.gateway.GatewayClientFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class InvitationNotificationServiceImpl(private val gatewayClientFactory: GatewayClientFactory) : IInvitationNotificationService {

    private val log = customLogger()

    override fun sendReinviteNotification(groupId: UUID, targetUserId: UUID, invitedByUserId: UUID) {


        log.error("TODO: Implement notification logic - Reinvite notification for user $targetUserId to group $groupId, invited by $invitedByUserId")
    }

    override fun sendAcceptInvitationNotification(groupId: UUID, userId: UUID) {
        TODO("Not yet implemented")
    }

    override fun sendInvitationNotification(
        invitationRequest: InvitationRequest,
        targetUser: InternalUserDTO,
        invitedByUser: InternalUserDTO,
        token: String
    ) {
        val message = BusinessExceptionType.buildJsonMessage("User invited to group", "Usuario invitado al grupo")
        val subject = BusinessExceptionType.buildJsonMessage("User invited to group", "Usuario invitado al grupo")
        val request = SendNotificationRequest.builder().message(message
        ).subject(subject).recipientEmail(targetUser.email).recipientId(targetUser.id)
            .senderEmail(invitedByUser.email)
            .senderUserId(invitedByUser.id)
            .emailTemplate(   "TEAM_INVITATION")
            .metadata()
            .build()

        var headers =  HashMap<String,String>()
        headers["Authorization"] = token

        log.info("Sending Email Notification for user ${targetUser.id} to group ${invitationRequest.teamID}  : ${request}: ${headers}")
        val httpResponse = gatewayClientFactory.getNotificationClient().sendEmailNotification(request, headers)

        log.info("Result HTTP Response: ${httpResponse}")
    }
}
