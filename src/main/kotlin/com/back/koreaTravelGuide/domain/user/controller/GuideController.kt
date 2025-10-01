package com.back.koreaTravelGuide.domain.guide.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.common.security.getUserId
import com.back.koreaTravelGuide.domain.guide.service.GuideService
import com.back.koreaTravelGuide.domain.user.dto.request.GuideUpdateRequest
import com.back.koreaTravelGuide.domain.user.dto.response.GuideResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "가이드 API", description = "가이드 조회 및 프로필 관리에 대한 API")
@RestController
@RequestMapping("/api/guides")
class GuideController(
    private val guideService: GuideService,
) {
    @Operation(summary = "가이드 목록 조회")
    @GetMapping
    fun getAllGuides(): ResponseEntity<ApiResponse<List<GuideResponse>>> {
        val guides = guideService.getAllGuides()
        return ResponseEntity.ok(ApiResponse("전체 가이드 목록을 조회했습니다.", guides))
    }

    @Operation(summary = "가이드 단건 조회")
    @GetMapping("/{guideId}")
    fun getGuideById(
        @PathVariable guideId: Long,
    ): ResponseEntity<ApiResponse<GuideResponse>> {
        val guide = guideService.getGuideById(guideId)
        return ResponseEntity.ok(ApiResponse("가이드 정보를 성공적으로 조회했습니다.", guide))
    }

    @Operation(summary = "가이드 프로필 수정")
    @PreAuthorize("hasRole('GUIDE')")
    @PatchMapping("/me")
    fun updateMyGuideProfile(
        authentication: Authentication,
        @RequestBody request: GuideUpdateRequest,
    ): ResponseEntity<ApiResponse<GuideResponse>> {
        val guideId = authentication.getUserId()
        val updatedGuideProfile = guideService.updateGuideProfile(guideId, request)
        return ResponseEntity.ok(ApiResponse("가이드 정보가 성공적으로 수정되었습니다.", updatedGuideProfile))
    }
}
