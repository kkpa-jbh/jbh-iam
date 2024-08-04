package com.jbh.iam.security.service

import com.jbh.iam.core.access.UserDataAccess
import com.jbh.iam.core.security.UserPrincipal
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*


/**
 * To authenticate a User or perform various role-based checks, Spring security needs to load users details somehow.
 *
 * For this purpose, It consists of an interface called UserDetailsService which has a single method that loads a user based on username-
 *
 * Note that, the loadUserByUsername() method returns a UserDetails object that Spring Security uses for performing various authentication and role based validations.

In our implementation, We’ll also define a custom UserPrincipal class that will implement UserDetails interface, and return the UserPrincipal object from loadUserByUsername() method.
 *
 */
@Service
class CustomUserDetailsService : UserDetailsService {

    @Autowired
    lateinit var userDataAccess: UserDataAccess

    companion object {
        val log: Logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)
    }

    /**
     * It's used by Spring Security when the user try to login.
     */
    override fun loadUserByUsername(userNameOrEmail: String): UserDetails {
        log.info("loading user by userName...  ${userNameOrEmail}")
        val user = userDataAccess.findByNickNameOrEmail(userNameOrEmail, userNameOrEmail)
            ?: throw UsernameNotFoundException("User not found with username or email $userNameOrEmail")

        log.info("loading user by userName... User Found! ${user.nickName}  ${user.email}")

        return UserPrincipal.create(user)
    }

    /**
     * This method is executed when a client consume and enpoint once its logged.
     * This method is used by JWTAuthenticationFilter.
     */
    fun loadUserById(id: UUID): UserDetails {
        log.info("Loading User by Id... ${id}")
        val userGroup = userDataAccess.findByUserOwnerId(id)
            ?: throw UsernameNotFoundException("User not found with id : " + id!!)

        log.info("Loading User by Id... User Found! ${UserPrincipal.create(userGroup)}")
        return UserPrincipal.create(userGroup)
    }
}