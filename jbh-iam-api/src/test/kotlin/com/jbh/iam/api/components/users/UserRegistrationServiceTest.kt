package com.jbh.iam.api.components.users

import com.jbh.iam.api.services.users.UserService
import com.jbh.iam.common.payload.SignUpRequest
import com.jbh.iam.core.model.RoleName
import com.jbh.iam.core.security.IPasswordEncoder
import com.jbh.iam.domain.modules.user.model.InternalUserDTO
import com.jbh.iam.domain.modules.user.model.RoleEntity
import com.jbh.iam.domain.modules.user.model.UserEntity
import com.jbh.iam.domain.modules.user.repository.RoleRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import java.util.Optional

class UserRegistrationServiceTest {

    private val userService = mock(UserService::class.java)
    private val roleRepository = mock(RoleRepository::class.java)
    private val passwordEncoder = mock(IPasswordEncoder::class.java)
    private val service = UserRegistrationService(userService, roleRepository, passwordEncoder)

    private fun request(password: String = "long-enough", username: String = "ana") =
        SignUpRequest(name = "Ana", username = username, email = "ana@example.com", password = password)

    @Test
    fun `password shorter than 8 characters is refused`() {
        assertThatThrownBy { service.registerUser(request(password = "1234567")) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("8")
        verifyNoInteractions(passwordEncoder)
    }

    @Test
    fun `blank username is refused`() {
        assertThatThrownBy { service.registerUser(request(username = " ")) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `taken nickname is refused`() {
        `when`(userService.existsByNickName("ana")).thenReturn(true)

        assertThatThrownBy { service.registerUser(request()) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `new user is saved with a hashed password and ROLE_USER`() {
        var saved: UserEntity? = null
        `when`(passwordEncoder.encode("long-enough")).thenReturn("hashed")
        `when`(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(RoleEntity(name = RoleName.ROLE_USER)))
        // any() returns null; the elvis gives Kotlin a non-null value while Mockito keeps the matcher.
        `when`(userService.save(any(UserEntity::class.java) ?: UserEntity())).thenAnswer {
            saved = it.getArgument(0)
            InternalUserDTO()
        }

        service.registerUser(request())

        assertThat(saved!!.password).isEqualTo("hashed")
        assertThat(saved!!.roles.map { it.name }).containsExactly(RoleName.ROLE_USER)
    }
}
