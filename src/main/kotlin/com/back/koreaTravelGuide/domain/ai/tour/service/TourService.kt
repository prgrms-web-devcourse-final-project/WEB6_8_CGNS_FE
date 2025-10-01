package com.back.koreaTravelGuide.domain.ai.tour.service

import com.back.koreaTravelGuide.domain.ai.tour.client.TourApiClient
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourDetailItem
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourDetailParams
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourDetailResponse
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourItem
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourLocationBasedParams
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourParams
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourResponse
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

// 09.26 양현준
@Service
class TourService(
    private val tourApiClient: TourApiClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // 파라미터를 TourParams DTO에 맞게 파싱
    fun parseParams(
        contentTypeId: String,
        areaAndSigunguCode: String,
    ): TourParams {
        val codes = areaAndSigunguCode.split(",").map { it.trim() }

        val areaCode = codes.getOrNull(0)
        val sigunguCode = codes.getOrNull(1)

        return TourParams(
            contentTypeId = contentTypeId,
            areaCode = areaCode,
            sigunguCode = sigunguCode,
        )
    }

    // API 호출 1, 지역기반 관광정보 조회 - areaBasedList2
    @Cacheable(
        "tourAreaBased",
        key = "#tourParams.contentTypeId + '_' + #tourParams.areaCode + '_' + #tourParams.sigunguCode",
    )
    fun fetchTours(tourParams: TourParams): TourResponse {
        // 09.30 테스트용 하드코딩
        if (
            tourParams.contentTypeId == "12" &&
            tourParams.areaCode == "6" &&
            tourParams.sigunguCode == "10"
        ) {
            return PRESET_AREA_TOUR_RESPONSE
        }

        val tours = tourApiClient.fetchTourInfo(tourParams)

        return tours
    }

    // API 호출 2, 위치기반 관광정보 조회 - locationBasedList2
    @Cacheable(
        "tourLocationBased",
        key =
            "#tourParams.contentTypeId + '_' + #tourParams.areaCode + '_' + #tourParams.sigunguCode + " +
                "'_' + #locationParams.mapX + '_' + #locationParams.mapY + '_' + #locationParams.radius",
    )
    fun fetchLocationBasedTours(
        tourParams: TourParams,
        locationParams: TourLocationBasedParams,
    ): TourResponse {
        // 09.30 테스트용 하드코딩
        if (
            tourParams.contentTypeId == "39" &&
            tourParams.areaCode == "1" &&
            tourParams.sigunguCode == "24" &&
            locationParams.mapX == "126.98375" &&
            locationParams.mapY == "37.563446" &&
            locationParams.radius == "100"
        ) {
            return PRESET_LOCATION_BASED_RESPONSE
        }

        return tourApiClient.fetchLocationBasedTours(tourParams, locationParams)
    }

    // APi 호출 3, 관광정보 상세조회 - detailCommon2
    @Cacheable("tourDetail", key = "#detailParams.contentId")
    fun fetchTourDetail(detailParams: TourDetailParams): TourDetailResponse {
        // 09.30 테스트용 하드코딩
        if (
            detailParams.contentId == "127974"
        ) {
            return PRESET_DETAIL_RESPONSE
        }

        return tourApiClient.fetchTourDetail(detailParams)
    }
}

/**
 * 09.30 테스트용 하드코딩
 * "areacode": "6" 부산
 * "sigungucode": "10" 사하구
 * "contenttypeid": "12" 관광지
 *  실제 API 호출 대신, 정해진 응답을 반환
 */
private val PRESET_AREA_TOUR_RESPONSE =
    TourResponse(
        items =
            listOf(
                TourItem(
                    contentId = "127974",
                    contentTypeId = "12",
                    createdTime = "20031208090000",
                    modifiedTime = "20250411180037",
                    title = "을숙도 공원",
                    addr1 = "부산광역시 사하구 낙동남로 1240 (하단동)",
                    areaCode = "6",
                    firstimage = "http://tong.visitkorea.or.kr/cms/resource/62/2487962_image2_1.jpg",
                    firstimage2 = "http://tong.visitkorea.or.kr/cms/resource/62/2487962_image3_1.jpg",
                    mapX = "128.9460030322",
                    mapY = "35.1045320626",
                    distance = null,
                    mlevel = "6",
                    sigunguCode = "10",
                    lDongRegnCd = "26",
                    lDongSignguCd = "380",
                ),
            ),
    )

private val PRESET_LOCATION_BASED_RESPONSE =
    TourResponse(
        items =
            listOf(
                TourItem(
                    contentId = "133858",
                    contentTypeId = "39",
                    createdTime = "20030529090000",
                    modifiedTime = "20250409105941",
                    title = "백제삼계탕",
                    addr1 = "서울특별시 중구 명동8길 8-10 (명동2가)",
                    areaCode = "1",
                    firstimage = "http://tong.visitkorea.or.kr/cms/resource/85/3108585_image2_1.JPG",
                    firstimage2 = "http://tong.visitkorea.or.kr/cms/resource/85/3108585_image3_1.JPG",
                    mapX = "126.9841178194",
                    mapY = "37.5634241535",
                    distance = "32.788938679922325",
                    mlevel = "6",
                    sigunguCode = "24",
                    lDongRegnCd = "11",
                    lDongSignguCd = "140",
                ),
            ),
    )

private val PRESET_DETAIL_RESPONSE =
    TourDetailResponse(
        items =
            listOf(
                TourDetailItem(
                    contentId = "126128",
                    title = "동촌유원지",
                    overview =
                        "동촌유원지는 대구시 동쪽 금호강변에 있는 44만 평의 유원지로 오래전부터 대구 시민이 즐겨 찾는 곳이다. " +
                            "각종 위락시설이 잘 갖춰져 있으며, 드라이브를 즐길 수 있는 도로가 건설되어 있다. 수량이 많은 금호강에는 조교가 가설되어 있고, " +
                            "우아한 다리 이름을 가진 아양교가 걸쳐 있다. 금호강(琴湖江)을 끼고 있어 예로부터 봄에는 그네뛰기, 봉숭아꽃 구경, " +
                            "여름에는 수영과 보트 놀이, 가을에는 밤 줍기 등 즐길 거리가 많은 곳이다. 또한, 해맞이다리, 유선장, 체육시설, " +
                            "실내 롤러스케이트장 등 다양한 즐길 거리가 있어 여행의 재미를 더해준다.",
                    addr1 = "대구광역시 동구 효목동",
                    mapX = "128.6506352387",
                    mapY = "35.8826195757",
                    firstImage = "http://tongit g.visitkorea.or.kr/cms/resource/86/3488286_image2_1.JPG",
                    tel = "",
                    homepage =
                        "",
                ),
            ),
    )
