package com.jbh.iam.domain.modules.usergroup.service

import com.jbh.iam.api.services.users.UserService
import com.jbh.iam.common.exceptions.ForbiddenException
import com.jbh.iam.domain.modules.user.model.InternalUserDTO
import com.jbh.iam.domain.modules.usergroup.model.UsersGroupEntity
import com.jbh.iam.domain.modules.usergroup.repository.UserGroupRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import java.util.Optional
import java.util.UUID

/** Access rules of user groups: who may read, change, and remove members. */
class UserGroupServiceImplTest {

    private val groupId = UUID.randomUUID()
    private val owner = UUID.randomUUID()
    private val member = UUID.randomUUID()
    private val stranger = UUID.randomUUID()

    private val repository = mock(UserGroupRepository::class.java)
    private val userService = mock(UserService::class.java)
    private val membershipService = mock(IUserGroupMembershipService::class.java)
    private val service = UserGroupServiceImpl(repository, userService, membershipService)

    @BeforeEach
    fun setUp() {
        val group = UsersGroupEntity(id = groupId, shortName = "fam", name = "Family", createdBy = owner)
        `when`(repository.findById(groupId)).thenReturn(Optional.of(group))
        `when`(userService.findByEmail("owner@example.com")).thenReturn(InternalUserDTO(id = owner))
        `when`(userService.findByEmail("member@example.com")).thenReturn(InternalUserDTO(id = member))
    }

    @Test
    fun `requireOwner accepts the creator and rejects anyone else`() {
        assertThat(service.requireOwner(groupId, owner).id).isEqualTo(groupId)

        assertThatThrownBy { service.requireOwner(groupId, member) }.isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun `requireOwner on an unknown group fails with not found`() {
        val unknown = UUID.randomUUID()
        `when`(repository.findById(unknown)).thenReturn(Optional.empty())

        assertThatThrownBy { service.requireOwner(unknown, owner) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `requireMember accepts accepted members only`() {
        `when`(membershipService.isAcceptedMember(groupId, member)).thenReturn(true)
        `when`(membershipService.isAcceptedMember(groupId, stranger)).thenReturn(false)

        service.requireMember(groupId, member)
        assertThatThrownBy { service.requireMember(groupId, stranger) }.isInstanceOf(ForbiddenException::class.java)
    }

    @Test
    fun `owner can remove a member`() {
        service.removeMemberFromGroup(groupId, "member@example.com", owner)

        verify(membershipService).removeMember(groupId, member)
    }

    @Test
    fun `member can remove themself`() {
        service.removeMemberFromGroup(groupId, "member@example.com", member)

        verify(membershipService).removeMember(groupId, member)
    }

    @Test
    fun `stranger cannot remove a member`() {
        assertThatThrownBy { service.removeMemberFromGroup(groupId, "member@example.com", stranger) }
            .isInstanceOf(ForbiddenException::class.java)
        verifyNoInteractions(membershipService)
    }

    @Test
    fun `nobody can remove the owner, not even the owner`() {
        assertThatThrownBy { service.removeMemberFromGroup(groupId, "owner@example.com", owner) }
            .isInstanceOf(ForbiddenException::class.java)
        verifyNoInteractions(membershipService)
    }
}
