package com.back.koreaTravelGuide.domain.auth.service

import com.back.koreaTravelGuide.common.security.JwtTokenProvider
import com.back.koreaTravelGuide.domain.auth.dto.response.LoginResponse
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${jwt.refresh-token-expiration-days}") private val refreshTokenExpirationDays: Long,
) {
    fun updateRoleAndLogin(
        userId: Long,
        role: UserRole,
    ): LoginResponse {
        if (role != UserRole.USER && role != UserRole.GUIDE) {
            throw IllegalArgumentException("선택할 수 없는 역할입니다.")
        }

        val user =
            userRepository.findById(userId)
                .orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다.") }

        if (user.role != UserRole.PENDING) {
            throw IllegalStateException("이미 역할이 설정된 사용자입니다.")
        }

        user.role = role
        userRepository.save(user)

        val accessToken = jwtTokenProvider.createAccessToken(user.id!!, user.role)

        return LoginResponse(accessToken = accessToken)
    }

    fun logout(accessToken: String) {
        val remainingTime = jwtTokenProvider.getRemainingTime(accessToken)

        if (remainingTime > 0) {
            redisTemplate.opsForValue().set(accessToken, "logout", remainingTime, TimeUnit.MILLISECONDS)
        }
    }

    fun refreshAccessToken(refreshToken: String): Pair<String, String> {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw object : AuthenticationException("유효하지 않은 Refresh Token입니다.") {}
        }

        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)

        val redisKey = "refreshToken:$userId"
        val storedRefreshToken =
            redisTemplate.opsForValue().get(redisKey)
                ?: throw object : AuthenticationException("로그인 정보가 만료되었습니다.") {}

        if (storedRefreshToken != refreshToken) {
            throw object : AuthenticationException("토큰 정보가 일치하지 않습니다.") {}
        }

        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("사용자를 찾을 수 없습니다.") }
        val newAccessToken = jwtTokenProvider.createAccessToken(user.id!!, user.role)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(user.id!!)

        redisTemplate.opsForValue().set(redisKey, newRefreshToken, refreshTokenExpirationDays, TimeUnit.DAYS)

        return Pair(newAccessToken, newRefreshToken)
    }
}
