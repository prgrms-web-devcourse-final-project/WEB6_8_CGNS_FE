package com.back.koreaTravelGuide.domain.ai.tour.dto

/**
 * 9.27 양현준
 * API 요청 파라미터
 * 기능상, 생략 가능한 필드는 생략 (arrange : 제목 순, cat : 대,중,소 분류, crpyrhtDivCd: 저작권유형)
 */

data class TourSearchParams(
    // 한 페이지 데이터 수, 10으로 지정
    val numOfRows: Int = DEFAULT_ROWS,
    // 페이지 번호, 1로 지정
    val pageNo: Int = DEFAULT_PAGE,
    // 관광타입 ID, 미 입력시 전체 조회 (12:관광지, 38 : 쇼핑...),
    val contentTypeId: String? = null,
    // 지역코드, 미 입력시 지역 전체 조회 (1:서울, 2:인천...)
    val areaCode: String? = null,
    // 시군구코드, 미 입력시 전체 조회
    val sigunguCode: String? = null,
) {
    companion object {
        const val DEFAULT_ROWS = 10
        const val DEFAULT_PAGE = 1
    }
}
