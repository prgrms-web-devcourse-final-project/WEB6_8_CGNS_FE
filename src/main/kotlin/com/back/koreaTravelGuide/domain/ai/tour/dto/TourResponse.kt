package com.back.koreaTravelGuide.domain.ai.tour.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 9.27 양현준
 * 관광 정보 응답 DTO
 * API 매뉴얼에서 필수인 값은 NonNull로 지정.
 */

data class TourResponse(
    val items: List<TourItem>,
)

// 관광 정보 단일 아이템
data class TourItem(
    // 콘텐츠ID (고유 번호, NonNull)
    val contentId: String,
    // 관광타입 ID (12: 관광지, NonNull)
    val contentTypeId: String,
    // 등록일 (NonNull)
    val createdTime: String,
    // 수정일 (NonNull)
    val modifiedTime: String,
    // 제목 (NonNull)
    val title: String,
    // 주소
    val addr1: String?,
    // 지역코드
    val areaCode: String?,
    // 이미지 (URL)
    val firstimage: String?,
    // 썸네일 이미지 (URL)
    val firstimage2: String?,
    // 경도
    val mapX: String?,
    // 위도
    val mapY: String?,
    // 거리 (위치 기반 조회 시 반환)
    val distance: String?,
    // 지도 레벨
    val mlevel: String?,
    // 시군구코드
    val sigunguCode: String?,
    // 법정동 시도 코드
    @get:JsonProperty("lDongRegnCd")
    val lDongRegnCd: String?,
    // 법정동 시군구 코드
    @get:JsonProperty("lDongSignguCd")
    val lDongSignguCd: String?,
)
