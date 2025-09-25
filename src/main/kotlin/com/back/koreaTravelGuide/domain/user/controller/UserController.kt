package com.back.koreaTravelGuide.domain.user.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.domain.user.dto.request.UserUpdateRequest
import com.back.koreaTravelGuide.domain.user.dto.response.UserResponse
import com.back.koreaTravelGuide.domain.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/api/users/me")
    fun getMyProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<ApiResponse<UserResponse>> {
        // username 이메일로 설정할것임
        val userProfile = userService.getUserProfileByEmail(userDetails.username)
        val response = ApiResponse("내 정보를 성공적으로 조회했습니다.", userProfile)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/api/users/me")
    fun updateMyProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: UserUpdateRequest,
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val updatedProfile = userService.updateProfile(userDetails.username, request)
        val response = ApiResponse("정보가 성공적으로 수정되었습니다.", updatedProfile)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/api/users/me")
    fun deleteUser(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.deleteUser(userDetails.username)
        return ResponseEntity.ok(ApiResponse("회원 탈퇴가 완료되었습니다."))
    }

    @GetMapping("/api/guides")
    fun getAllGuides(): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val guides = userService.getAllGuides()
        val response = ApiResponse("전체 가이드 목록을 조회했습니다.", guides)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/api/guides/{guideId}")
    fun getGuideById(
        @PathVariable guideId: Long,
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val guide = userService.getGuideById(guideId)
        val response = ApiResponse("가이드 정보를 성공적으로 조회했습니다.", guide)
        return ResponseEntity.ok(response)
    }
}
