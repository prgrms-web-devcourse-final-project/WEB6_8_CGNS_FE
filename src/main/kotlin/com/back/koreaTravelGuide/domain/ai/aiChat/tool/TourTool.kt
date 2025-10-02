package com.back.koreaTravelGuide.domain.ai.aiChat.tool

import com.back.backend.BuildConfig
import com.back.koreaTravelGuide.common.logging.log
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourDetailParams
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourLocationBasedParams
import com.back.koreaTravelGuide.domain.ai.tour.service.TourService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class TourTool(
    private val tourService: TourService,
    private val objectMapper: ObjectMapper,
) {
    /**
     * fetchTours - 지역기반 관광정보 조회
     * 케이스 : 부산광역시 사하구에 있는 관광지 조회
     * "areacode": "6" 부산
     * "sigungucode": "10" 사하구
     * "contenttypeid": "12" 관광지
     */

    @Tool(description = "areaBasedList2 : 지역기반 관광정보 조회, 특정 지역의 관광 정보 조회")
    fun getAreaBasedTourInfo(
        @ToolParam(
            description =
                "관광 타입 코드를 사용하세요. 사용자가 타입 이름을 말하면 해당하는 코드를 찾아서 사용해야 합니다. " +
                    "예: 사용자가 '관광지'라고 하면 '12'를 사용하세요. " +
                    "사용 가능한 타입 코드: ${BuildConfig.CONTENT_TYPE_CODES_DESCRIPTION}",
            required = true,
        )
        contentTypeId: String,
        @ToolParam(
            description =
                "지역 코드를 쉼표(,)로 구분해서 사용하세요. " +
                    "예: 사용자가 '서울 강남구'라고 하면 AREA_CODES에서 '서울-강남구: 1-1'을 찾고, " +
                    "하이픈(-)을 쉼표(,)로 바꿔서 '1,1'을 사용하세요. " +
                    "광역시(인천, 대전 등)는 단일 코드만 사용: 예: '인천' → '2' (쉼표 없음). " +
                    "사용 가능한 지역 코드: ${BuildConfig.AREA_CODES_DESCRIPTION}",
            required = true,
        )
        areaAndSigunguCode: String,
    ): String {
        log.info("🔧 [TOOL CALLED] getAreaBasedTourInfo - contentTypeId: $contentTypeId, areaAndSigunguCode: $areaAndSigunguCode")

        val tourParams = tourService.parseParams(contentTypeId, areaAndSigunguCode)
        val tourInfo = tourService.fetchTours(tourParams)

        return try {
            val result = tourInfo.let { objectMapper.writeValueAsString(it) }
            log.info("✅ [TOOL RESULT] getAreaBasedTourInfo - 결과: ${result.take(100)}...")
            result
        } catch (e: Exception) {
            log.error("❌ [TOOL ERROR] getAreaBasedTourInfo - 예외 발생", e)
            "지역기반 관광정보 조회를 가져올 수 없습니다."
        }
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
    fun getLocationBasedTourInfo(
        @ToolParam(
            description =
                "관광 타입 코드를 사용하세요. 사용자가 타입 이름을 말하면 해당하는 코드를 찾아서 사용해야 합니다. " +
                    "예: 사용자가 '음식점'이라고 하면 '39'를 사용하세요. " +
                    "사용 가능한 타입 코드: ${BuildConfig.CONTENT_TYPE_CODES_DESCRIPTION}",
            required = true,
        )
        contentTypeId: String,
        @ToolParam(
            description =
                "지역 코드를 쉼표(,)로 구분해서 사용하세요. " +
                    "예: 사용자가 '서울 중구'라고 하면 AREA_CODES에서 '서울-중구: 1-24'를 찾고, " +
                    "하이픈(-)을 쉼표(,)로 바꿔서 '1,24'를 사용하세요. " +
                    "사용 가능한 지역 코드: ${BuildConfig.AREA_CODES_DESCRIPTION}",
            required = true,
        )
        areaAndSigunguCode: String,
        @ToolParam(description = "WGS84 경도 좌표", required = true)
        mapX: String = "126.98375",
        @ToolParam(description = "WGS84 위도 좌표", required = true)
        mapY: String = "37.563446",
        @ToolParam(description = "검색 반경(미터 단위)", required = true)
        radius: String = "100",
    ): String {
        log.info(
            "🔧 [TOOL CALLED] getLocationBasedTourInfo - " +
                "contentTypeId: $contentTypeId, area: $areaAndSigunguCode, " +
                "mapX: $mapX, mapY: $mapY, radius: $radius",
        )

        val tourParams = tourService.parseParams(contentTypeId, areaAndSigunguCode)
        val locationBasedParams = TourLocationBasedParams(mapX, mapY, radius)
        val tourLocationBasedInfo = tourService.fetchLocationBasedTours(tourParams, locationBasedParams)

        return try {
            val result = tourLocationBasedInfo.let { objectMapper.writeValueAsString(it) }
            log.info("✅ [TOOL RESULT] getLocationBasedTourInfo - 결과: ${result.take(100)}...")
            result
        } catch (e: Exception) {
            log.error("❌ [TOOL ERROR] getLocationBasedTourInfo - 예외 발생", e)
            "위치기반 관광정보 조회를 가져올 수 없습니다."
        }
    }

    /**
     * fetchTourDetail - 상세조회
     * 케이스 : 콘텐츠ID가 "126128"인 관광정보의 "상베 정보" 조회
     * "contentid": "127974",
     */

    @Tool(description = "detailCommon2 : 관광정보 상세조회, 특정 관광 정보의 상세 정보 조회")
    fun getTourDetailInfo(
        @ToolParam(
            description =
                "조회할 관광정보의 콘텐츠 ID. " +
                    "이전 Tool 호출 결과(getAreaBasedTourInfo 또는 getLocationBasedTourInfo)에서 받은 contentId를 사용하세요.",
            required = true,
        )
        contentId: String = "127974",
    ): String {
        log.info("🔧 [TOOL CALLED] getTourDetailInfo - contentId: $contentId")

        val tourDetailParams = TourDetailParams(contentId)
        val tourDetailInfo = tourService.fetchTourDetail(tourDetailParams)

        return try {
            val result = tourDetailInfo.let { objectMapper.writeValueAsString(it) }
            log.info("✅ [TOOL RESULT] getTourDetailInfo - 결과: ${result.take(100)}...")
            result
        } catch (e: Exception) {
            log.error("❌ [TOOL ERROR] getTourDetailInfo - 예외 발생", e)
            "관광정보 상세조회를 가져올 수 없습니다."
        }
    }
}
