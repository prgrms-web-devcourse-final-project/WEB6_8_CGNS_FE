package com.back.koreaTravelGuide.security

import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CustomOAuth2LoginSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
) : SimpleUrlAuthenticationSuccessHandler() {
    @Transactional
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val oAuth2User = authentication.principal as OAuth2User
        val email = oAuth2User.attributes["email"] as String

        val user = userRepository.findByEmail(email)!!

        if (user.role == UserRole.PENDING) {
            val registerToken = jwtTokenProvider.createRegisterToken(user.id!!)
            val targetUrl = "http://localhost:3000/signup/role?token=$registerToken"
            redirectStrategy.sendRedirect(request, response, targetUrl)
        } else {
            val accessToken = jwtTokenProvider.createAccessToken(user.id!!, user.role)
            val targetUrl = "http://localhost:3000/oauth/callback?accessToken=$accessToken"
            redirectStrategy.sendRedirect(request, response, targetUrl)
        }
    }
}
