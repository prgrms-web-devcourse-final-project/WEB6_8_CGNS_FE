package com.back.koreaTravelGuide.domain.ai.aiChat.tool

import com.back.backend.BuildConfig
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourDetailParams
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourLocationBasedParams
import com.back.koreaTravelGuide.domain.ai.tour.service.TourService
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class TourToolExample(
    private val tourService: TourService,
) {
    /**
     * fetchTours - 지역기반 관광정보 조회
     * 케이스 : 부산광역시 사하구에 있는 관광지 조회
     * "areacode": "6" 부산
     * "sigungucode": "10" 사하구
     * "contenttypeid": "12" 관광지
     */

    @Tool(description = "areaBasedList2 : 지역기반 관광정보 조회, 특정 지역의 관광 정보 조회")
    fun getTourInfo(
        @ToolParam(description = BuildConfig.CONTENT_TYPE_CODES_DESCRIPTION, required = true)
        contentTypeId: String,
        @ToolParam(description = BuildConfig.AREA_CODES_DESCRIPTION, required = true)
        areaAndSigunguCode: String,
    ): String {
        // areaAndSigunguCode를 areaCode와 sigunguCode로 분리
        val tourParams = tourService.parseParams(contentTypeId, areaAndSigunguCode)

        val tourInfo = tourService.fetchTours(tourParams)

        return tourInfo.toString() ?: "지역기반 관광정보 조회를 가져올 수 없습니다."
    }

    /**
     * fetchLocationBasedTours - 위치기반 관광정보 조회
     * 케이스 : 서울특별시 중구 명동 근처 100m 이내에있는 음식점 조회
     * "areacode": "1" 서울
     * "sigungucode": "24" 중구
     * "contenttypeid": "39" 음식점
     * "mapx": "126.98375",
     * "mapy": "37.563446",
     * "radius": "100",
     */

    @Tool(description = "locationBasedList2 : 위치기반 관광정보 조회, 특정 위치 기반의 관광 정보 조회")
    fun get(
        @ToolParam(description = BuildConfig.CONTENT_TYPE_CODES_DESCRIPTION, required = true)
        contentTypeId: String,
        @ToolParam(description = BuildConfig.AREA_CODES_DESCRIPTION, required = true)
        areaAndSigunguCode: String,
        @ToolParam(description = "WGS84 경도", required = true)
        mapX: String = "126.98375",
        @ToolParam(description = "WGS84 위도", required = true)
        mapY: String = "37.563446",
        @ToolParam(description = "검색 반경(m)", required = true)
        radius: String = "100",
    ): String {
        // areaAndSigunguCode를 areaCode와 sigunguCode로 분리
        val tourParams = tourService.parseParams(contentTypeId, areaAndSigunguCode)
        val locationBasedParams = TourLocationBasedParams(mapX, mapY, radius)

        val tourLocationBasedInfo = tourService.fetchLocationBasedTours(tourParams, locationBasedParams)

        return tourLocationBasedInfo.toString() ?: "위치기반 관광정보 조회를 가져올 수 없습니다."
    }

    /**
     * fetchTourDetail - 상세조회
     * 케이스 : 콘텐츠ID가 “126128”인 관광정보의 “상베 정보” 조회
     * "contentid": "127974",
     */

    @Tool(description = "detailCommon2 : 관광정보 상세조회, 특정 관광 정보의 상세 정보 조회")
    fun get(
        @ToolParam(description = "Tour API Item에 각각 할당된 contentId", required = true)
        contentId: String = "127974",
    ): String {
        val tourDetailParams = TourDetailParams(contentId)

        val tourDetailInfo = tourService.fetchTourDetail(tourDetailParams)

        return tourDetailInfo.toString() ?: "관광정보 상세조회를 가져올 수 없습니다."
    }
}
