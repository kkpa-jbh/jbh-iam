package com.jbh.iam.domain.modules.usergroup.model

data class GroupMemberResponse(
    val nickName: String,
    val email: String,
    val status: InvitationStatus,
    val isGroupOwner: Boolean
)
