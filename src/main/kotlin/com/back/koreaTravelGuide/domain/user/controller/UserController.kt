package com.back.koreaTravelGuide.domain.user.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.common.security.getUserId
import com.back.koreaTravelGuide.domain.user.dto.request.UserUpdateRequest
import com.back.koreaTravelGuide.domain.user.dto.response.UserResponse
import com.back.koreaTravelGuide.domain.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    fun getMyProfile(authentication: Authentication): ResponseEntity<ApiResponse<UserResponse>> {
        val userId = authentication.getUserId()
        val userProfile = userService.getUserProfileById(userId)
        return ResponseEntity.ok(ApiResponse("내 정보를 성공적으로 조회했습니다.", userProfile))
    }

    @Operation(summary = "내 프로필 수정")
    @PatchMapping("/me")
    fun updateMyProfile(
        authentication: Authentication,
        @RequestBody request: UserUpdateRequest,
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val userId = authentication.getUserId()
        val updatedProfile = userService.updateUserProfile(userId, request)
        return ResponseEntity.ok(ApiResponse("정보가 성공적으로 수정되었습니다.", updatedProfile))
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    fun deleteMe(authentication: Authentication): ResponseEntity<ApiResponse<Unit>> {
        val userId = authentication.getUserId()
        userService.deleteUser(userId)
        return ResponseEntity.ok(ApiResponse("회원 탈퇴가 완료되었습니다."))
    }
}
