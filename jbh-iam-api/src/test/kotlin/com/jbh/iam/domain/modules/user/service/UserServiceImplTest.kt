package com.jbh.iam.api.services.users

import com.jbh.iam.api.exceptions.web.ResourceNotFoundException
import com.jbh.iam.cache.api.UserCacheOperations
import com.jbh.iam.domain.modules.user.model.UserEntity
import com.jbh.iam.domain.modules.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.UUID

class UserServiceImplTest {

    private val userRepository = mock(UserRepository::class.java)
    private val userCache = mock(UserCacheOperations::class.java)
    private val service = UserServiceImpl(userRepository, userCache)

    @Test
    fun `findByNickNameOrEmail uses the email on a cache miss`() {
        val userId = UUID.randomUUID()
        `when`(userRepository.findByNickNameOrEmail("ana", "ana@example.com"))
            .thenReturn(UserEntity(id = userId, nickName = "ana", email = "ana@example.com"))

        val user = service.findByNickNameOrEmail("ana", "ana@example.com")

        assertThat(user.id).isEqualTo(userId)
        verify(userRepository).findByNickNameOrEmail("ana", "ana@example.com")
    }

    @Test
    fun `findByNickNameOrEmail gives not found for an unknown user`() {
        assertThatThrownBy { service.findByNickNameOrEmail("nobody", "nobody@example.com") }
            .isInstanceOf(ResourceNotFoundException::class.java)
    }
}
