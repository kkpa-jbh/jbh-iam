package com.jbh.iam.domain.modules.usergroup.service

import java.util.*

interface IInvitationNotificationService {
    fun sendReinviteNotification(groupId: UUID, targetUserId: UUID, invitedByUserId: UUID)
}
