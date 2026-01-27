package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.common.logging.customLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class InvitationNotificationServiceImpl : IInvitationNotificationService {

    private val log = customLogger()

    override fun sendReinviteNotification(groupId: UUID, targetUserId: UUID, invitedByUserId: UUID) {
        log.error("TODO: Implement notification logic - Reinvite notification for user $targetUserId to group $groupId, invited by $invitedByUserId")
    }
}
