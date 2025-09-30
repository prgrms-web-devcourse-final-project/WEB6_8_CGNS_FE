package com.back.koreaTravelGuide.domain.auth.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.domain.auth.dto.request.UserRoleUpdateRequest
import com.back.koreaTravelGuide.domain.auth.dto.response.AccessTokenResponse
import com.back.koreaTravelGuide.domain.auth.dto.response.LoginResponse
import com.back.koreaTravelGuide.domain.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    @Value("\${jwt.refresh-token-expiration-days}") private val refreshTokenExpirationDays: Long,
) {
    @PostMapping("/refresh")
    fun refreshAccessToken(
        @CookieValue("refreshToken") refreshToken: String,
        response: HttpServletResponse,
    ): ResponseEntity<ApiResponse<AccessTokenResponse>> {
        val (newAccessToken, newRefreshToken) = authService.refreshAccessToken(refreshToken)

        val cookie =
            jakarta.servlet.http.Cookie("refreshToken", newRefreshToken).apply {
                isHttpOnly = true
                secure = true
                path = "/"
                maxAge = (refreshTokenExpirationDays * 24 * 60 * 60).toInt()
            }
        response.addCookie(cookie)

        return ResponseEntity.ok(ApiResponse("Access Token이 성공적으로 재발급되었습니다.", AccessTokenResponse(newAccessToken)))
    }

    @Operation(summary = "신규 사용자 역할 선택")
    @PostMapping("/role")
    fun updateUserRole(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: UserRoleUpdateRequest,
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val loginResponse = authService.updateRoleAndLogin(userId, request.role)
        return ResponseEntity.ok(ApiResponse("역할이 선택되었으며 로그인에 성공했습니다.", loginResponse))
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest): ResponseEntity<ApiResponse<Unit>> {
        val token =
            request.getHeader("Authorization")?.substring(7)
                ?: throw IllegalArgumentException("토큰이 없습니다.")

        authService.logout(token)

        return ResponseEntity.ok(ApiResponse("로그아웃 되었습니다."))
    }
}
