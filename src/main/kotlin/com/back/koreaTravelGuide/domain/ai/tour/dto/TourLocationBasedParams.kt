package com.back.koreaTravelGuide.domain.ai.tour.dto

/**
 * 9.29 양현준
 * 위치기반 관광정보 조회 요청 파라미터 (locationBasedList2)
 * 필수 좌표 값(mapX, mapY, radius)은 NonNull로 정의해 호출 시점에 무조건 전달되도록 보장한다.
 */
data class TourLocationBasedParams(
    val mapX: String,
    val mapY: String,
    val radius: String,
)
