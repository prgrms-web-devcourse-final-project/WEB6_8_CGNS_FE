package com.back.koreaTravelGuide.domain.ai.tour.dto

// 09.25 양현준
// 관광 정보 호출용 파라미터
// 생략 가능한 필드는 생략 하였음 (arrange : 제목 순으로 정렬, cat : 대,중,소 분류, crpyrhtDivCd: 저작권유형)
data class TourSearchParams(
    val numOfRows: Int = 10, // 한 페이지 데이터 수, 미 입력시 10
    val pageNo: Int = 1, // 페이지 번호, 미 입력시 10

    val contentTypeId: String = "12", // 관광타입 ID, 미 입력시 전체 조회 (12:관광지, 38 : 쇼핑...), 우선 관광지로 하드코딩
    val areaCode: String, // 지역코드, 미 입력시 지역 전체 (1:서울, 2:인천...)
    val sigunguCode: String, // 시군구코드, 미 입력시 전체
)