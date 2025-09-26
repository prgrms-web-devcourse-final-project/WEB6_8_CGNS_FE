package com.back.koreaTravelGuide.domain.ai.tour.dto

/**
 * 9.25 양현준
 * 관광 정보 응답 DTO
 * API 매뉴얼에서 필수인 값은 NonNull로 지정.
 */
data class TourResponse(
    // 콘텐츠ID (고유 번호)
    val contentId: String,
    // 관광타입 ID (12: 관광지, 14: 문화시설 ..)
    val contentTypeId: String,
    // 등록일
    val createdTime: String,
    // 수정일
    val modifiedTime: String,
    // 제목
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
    // 지도 레벨
    val mlevel: String?,
    // 시군구코드
    val sigunguCode: String?,
    // 법정동 시도 코드, 응답 코드가 IDongRegnCd 이므로,
    val lDongRegnCd: String?,
    // 법정동 시군구 코드
    val lDongSignguCd: String?,
)
