package com.back.koreaTravelGuide.common.security

import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Component
class CustomOAuth2LoginSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${jwt.refresh-token-expiration-days}") private val refreshTokenExpirationDays: Long,
) : SimpleUrlAuthenticationSuccessHandler() {
    @Transactional
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val customUser = authentication.principal as CustomOAuth2User

        val email = customUser.email

        val user = userRepository.findByEmail(email)!!

        if (user.role == UserRole.PENDING) {
            val registerToken = jwtTokenProvider.createRegisterToken(user.id!!)

            val targetUrl = "http://localhost:3000/signup/role?token=$registerToken"

            redirectStrategy.sendRedirect(request, response, targetUrl)
        } else {
            val accessToken = jwtTokenProvider.createAccessToken(user.id!!, user.role)

            val refreshToken = jwtTokenProvider.createRefreshToken(user.id!!)

            val redisKey = "refreshToken:${user.id}"

            redisTemplate.opsForValue().set(redisKey, refreshToken, refreshTokenExpirationDays, TimeUnit.DAYS)

            val cookie =
                Cookie("refreshToken", refreshToken).apply {
                    isHttpOnly = true

                    secure = true

                    path = "/"

                    maxAge = (refreshTokenExpirationDays * 24 * 60 * 60).toInt()
                }

            response.addCookie(cookie)

            val targetUrl = "http://localhost:3000/oauth/callback?accessToken=$accessToken"

            redirectStrategy.sendRedirect(request, response, targetUrl)
        }
    }
}
