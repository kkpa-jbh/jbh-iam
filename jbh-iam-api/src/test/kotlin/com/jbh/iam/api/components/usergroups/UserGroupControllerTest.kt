package com.jbh.iam.api.components.usergroups

import com.jbh.iam.common.exceptions.ForbiddenException
import com.jbh.iam.core.access.AuthenticationOperationService
import com.jbh.iam.domain.modules.usergroup.service.IInvitationService
import com.jbh.iam.domain.modules.usergroup.service.UserGroupServiceImpl
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.util.UUID

/** The controller must run the access check before it changes anything. */
class UserGroupControllerTest {

    private val groupId = UUID.randomUUID()
    private val caller = UUID.randomUUID()

    private val groupService = mock(UserGroupServiceImpl::class.java)
    private val authService = mock(AuthenticationOperationService::class.java).also {
        `when`(it.getUserIdFromJWT("token")).thenReturn(caller)
    }
    private val controller = UserGroupController(groupService, mock(IInvitationService::class.java), authService)

    @Test
    fun `delete by a non-owner is refused and deletes nothing`() {
        doThrow(ForbiddenException("not owner")).`when`(groupService).requireOwner(groupId, caller)

        assertThatThrownBy { controller.deleteUserGroup(groupId, "Bearer token") }
            .isInstanceOf(ForbiddenException::class.java)
        verify(groupService, never()).deleteById(groupId)
    }

    @Test
    fun `invitations of a group are hidden from non-members`() {
        doThrow(ForbiddenException("not member")).`when`(groupService).requireMember(groupId, caller)

        assertThatThrownBy { controller.getGroupInvitations(groupId, "Bearer token") }
            .isInstanceOf(ForbiddenException::class.java)
    }
}
