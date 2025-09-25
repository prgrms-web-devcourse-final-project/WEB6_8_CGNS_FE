package com.back.koreaTravelGuide.domain.ai.tour.dto

// 09.25 양현준
// 관광 정보 응답 DTOd
// API 매뉴얼에서 필수로 받는 값은 NonNull로 지정.
data class TourResponse(
    val contentId: String, // 콘텐츠ID (고유 번호)
    val contentTypeId: String, // 관광타입 ID (12: 관광지, 14: 문화시설 ..)
    val createdTime: String, // 등록일
    val modifiedTime: String, // 수정일

    val title: String, // 제목
    val addr1 : String?, // 주소
    val areaCode : String?, // 지역코드
    val firstimage: String?, // 이미지 (URL)
    val firstimage2: String?, // 썸네일 이미지 (URL)
    val mapX: String?, // 경도
    val mapY: String?, // 위도
    val mlevel: String?, // 지도 레벨
    val sigunguCode: String?, // 시군구코드
    val lDongRegnCd: String?, // 법정동 시도 코드, 응답 코드가 IDongRegnCd 이므로,
    val lDongSignguCd: String? // 법정동 시군구 코드
)