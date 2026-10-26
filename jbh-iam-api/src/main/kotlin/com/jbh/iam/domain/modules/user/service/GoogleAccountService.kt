package com.jbh.iam.domain.modules.user.service

import com.jbh.iam.domain.modules.user.model.UserEntity
import com.jbh.iam.domain.modules.user.repository.UserRepository
import org.springframework.stereotype.Service

/** Data access for Google sign-in. It keeps the repository out of the web layer. */
@Service
class GoogleAccountService(private val userRepository: UserRepository) {

    fun findByGoogleSub(googleSub: String): UserEntity? = userRepository.findByGoogleSub(googleSub)

    fun findByEmail(email: String): UserEntity? = userRepository.findByEmail(email)

    fun existsByNickName(nickName: String): Boolean = userRepository.existsByNickName(nickName)

    fun save(user: UserEntity): UserEntity = userRepository.save(user)
}
