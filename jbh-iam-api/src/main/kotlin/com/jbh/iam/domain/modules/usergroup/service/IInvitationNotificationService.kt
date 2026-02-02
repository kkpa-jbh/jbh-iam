package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.domain.modules.user.model.InternalUserDTO
import com.jbh.iam.domain.modules.usergroup.model.InvitationRequest
import java.util.*

interface IInvitationNotificationService {
    fun sendAcceptInvitationNotification(groupId: UUID, userId: UUID)
    fun sendInvitationNotification(invitationRequest: InvitationRequest, targetUser: InternalUserDTO, invitedByUser: InternalUserDTO, token: String)
}
