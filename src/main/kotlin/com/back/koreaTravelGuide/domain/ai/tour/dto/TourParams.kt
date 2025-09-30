package com.back.koreaTravelGuide.domain.ai.tour.dto

/**
 * 9.27 양현준
 * 지역기반 관광정보 조회 요청 파라미터 (areaBasedList2)
 * 기능상, 생략 가능한 필드는 생략 (arrange : 제목 순, cat : 대,중,소 분류, crpyrhtDivCd: 저작권유형)
 * 관광타입 ID(12:관광지, 38 : 쇼핑...), 지역코드(1:서울, 2:인천...), 시군구코드(110:종로구, 140:강남구...), 미 입력시 전체 조회
 */

data class TourParams(
    val contentTypeId: String? = null,
    val areaCode: String? = null,
    val sigunguCode: String? = null,
)
