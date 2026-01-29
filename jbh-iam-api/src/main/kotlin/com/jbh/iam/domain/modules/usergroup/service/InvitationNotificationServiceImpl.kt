package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.common.logging.customLogger
import com.jbh.iam.domain.modules.user.model.InternalUserDTO
import com.jbh.iam.domain.modules.usergroup.model.InvitationRequest
import com.jbh.iam.gateway.GatewayClientFactory
import com.jbh.notification.contracts.NotificationType
import com.jbh.notification.contracts.SendNotificationRequest
import com.jbh.notification.contracts.email.templates.TeamInvitationMetadata
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

        val teamInvitationMetadata = TeamInvitationMetadata.builder()
            .teamId(invitationRequest.teamID)
            .teamName(invitationRequest.teamName)
            .inviterName(invitedByUser.email)
            .build()

        val request = SendNotificationRequest.builder().recipientEmail(targetUser.email).recipientId(targetUser.id)
            .senderEmail(invitedByUser.email)
            .senderUserId(invitedByUser.id)
            .templateMetadata(teamInvitationMetadata)
            .build()

        var headers = HashMap<String, String>()
        headers["Authorization"] = token

        log.info("Sending Email Notification for user ${targetUser.id} to group ${invitationRequest.teamID}  : ${request}: ${headers}")
        try {
            val httpResponse = gatewayClientFactory.getNotificationClient()
                .sendNotification(request, NotificationType.EMAIL, headers)
            log.info("Result HTTP Response: $httpResponse")
        } catch (e: Exception) {
            log.error("Failed to send notification for user ${targetUser.id}", e)
        }
    }
}
