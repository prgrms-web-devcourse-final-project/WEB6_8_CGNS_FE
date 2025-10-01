package com.back.koreaTravelGuide.domain.rate.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.common.security.getUserId
import com.back.koreaTravelGuide.domain.rate.dto.GuideRatingSummaryResponse
import com.back.koreaTravelGuide.domain.rate.dto.RateRequest
import com.back.koreaTravelGuide.domain.rate.dto.RateResponse
import com.back.koreaTravelGuide.domain.rate.service.RateService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rate")
class RateController(
    private val rateService: RateService,
) {
    @Operation(summary = "가이드 평가 생성/수정")
    @PutMapping("/guides/{guideId}")
    fun rateGuide(
        authentication: Authentication,
        @PathVariable guideId: Long,
        @RequestBody request: RateRequest,
    ): ResponseEntity<ApiResponse<RateResponse>> {
        val raterUserId = authentication.getUserId()
        val rate = rateService.rateGuide(raterUserId, guideId, request.rating, request.comment)
        return ResponseEntity.ok(ApiResponse("가이드 평가가 등록되었습니다.", RateResponse.from(rate)))
    }

    @Operation(summary = "AI 채팅 세션 평가 생성/수정")
    @PutMapping("/aichat/sessions/{sessionId}")
    fun rateAiSession(
        authentication: Authentication,
        @PathVariable sessionId: Long,
        @RequestBody request: RateRequest,
    ): ResponseEntity<ApiResponse<RateResponse>> {
        val raterUserId = authentication.getUserId()
        val rate = rateService.rateAiSession(raterUserId, sessionId, request.rating, request.comment)
        return ResponseEntity.ok(ApiResponse("AI 채팅 평가가 등록되었습니다.", RateResponse.from(rate)))
    }

    @Operation(summary = "내가 받은 가이드 평가 조회")
    @GetMapping("/guides/my")
    @PreAuthorize("hasRole('GUIDE')")
    fun getMyGuideRatings(authentication: Authentication): ResponseEntity<ApiResponse<GuideRatingSummaryResponse>> {
        val guideId = authentication.getUserId()
        val summary = rateService.getMyGuideRatingSummary(guideId)
        return ResponseEntity.ok(ApiResponse("내 가이드 평점 정보를 조회했습니다.", summary))
    }

    @Operation(summary = "관리자의 모든 AI 채팅 평가 조회")
    @GetMapping("/admin/aichat/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllAiSessionRatings(pageable: Pageable): ResponseEntity<ApiResponse<Page<RateResponse>>> {
        val ratingsPage = rateService.getAllAiSessionRatingsForAdmin(pageable)
        return ResponseEntity.ok(ApiResponse("모든 AI 채팅 평가 목록을 조회했습니다.", ratingsPage))
    }
}
