package com.back.koreaTravelGuide.domain.ai.tour.dto

// TODO: 관광 정보 응답 DTO - 관광지 정보 및 메타데이터 반환
data class TourResponse(
    val name: String,
    val location: String,
    val description: String,
)
