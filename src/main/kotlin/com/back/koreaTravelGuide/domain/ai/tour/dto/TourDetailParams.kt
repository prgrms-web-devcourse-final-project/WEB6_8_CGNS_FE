package com.back.koreaTravelGuide.domain.ai.tour.dto

/**
 * 공통정보(detailCommon2) 조회 요청 파라미터.
 * contentId는 필수, 페이지 관련 값은 기본값으로 1페이지/10건을 사용한다.
 */
data class TourDetailParams(
    val contentId: String,
)
