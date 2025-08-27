package com.jbh.iam.viewgateway.web.views

import com.jbh.iam.common.payload.LoginRequest
import com.jbh.iam.common.payload.SignUpRequest
import com.jbh.iam.viewgateway.web.service.ViewAuthService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/view/auth")
class ViewAuthController(private val viewAuthService: ViewAuthService) {

    private val log = LoggerFactory.getLogger(ViewAuthController::class.java)

    @GetMapping("/login")
    fun loginPage(): String {
        // References to templates folder from resources folder
        return "auth/login"
    }

    @GetMapping("/test")
    fun testPage(): String {
        log.info("Loading test page")
        return "test"
    }

    @PostMapping("/signin")
    fun loginUser(
        @ModelAttribute signInRequest: LoginRequest,
        model: Model,
        response: HttpServletResponse
    ): String {
        try {
            val (result, jwtCookie) = viewAuthService.loginUser(signInRequest)
            model.addAttribute("loginResult", result)
            log.info("Login result: $result with cookies: $jwtCookie")

            // Set the cookies in the response
            if (jwtCookie.isNotBlank()) {
                response.addHeader("Set-Cookie", jwtCookie)
            }

            return "auth/login :: #login-result"
        } catch (e: Exception) {
            log.error("Login failed", e)
            model.addAttribute("loginResult", "Login failed: ${e.message}")
            return "auth/login :: #login-result"
        }
    }

    @GetMapping("/register")
    fun registerPage(): String {
        return "auth/register"
    }

    @PostMapping("/register")
    fun registerUser(@ModelAttribute signUpRequest: SignUpRequest, model: Model): String {
        try {
            val result = viewAuthService.registerUser(signUpRequest)
            model.addAttribute("registrationResult", result)
            log.info("Registration result: $result")
            return "auth/register :: #registration-result"
        } catch (e: Exception) {
            log.error("Registration failed", e)
            model.addAttribute("registrationResult", "Registration failed: ${e.message}")
            return "auth/register :: #registration-result"
        }
    }

    @GetMapping("/check-username", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun checkUsername(@RequestParam username: String): String {
        val isAvailable = viewAuthService.checkUsernameAvailability(username)
        return if (isAvailable) {
            "<span style='color: green;'>Username is available</span>"
        } else {
            "<span style='color: red;'>Username is already taken</span>"
        }
    }

    @GetMapping("/check-email", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun checkEmail(@RequestParam email: String): String {
        val isAvailable = viewAuthService.checkEmailAvailability(email)
        return if (isAvailable) {
            "<span style='color: green;'>Email is available</span>"
        } else {
            "<span style='color: red;'>Email is already in use</span>"
        }
    }

}