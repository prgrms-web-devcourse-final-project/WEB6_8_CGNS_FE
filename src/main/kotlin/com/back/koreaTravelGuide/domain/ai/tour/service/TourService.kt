package com.back.koreaTravelGuide.domain.ai.tour.service

import com.back.koreaTravelGuide.domain.ai.tour.client.TourApiClient
import com.back.koreaTravelGuide.domain.ai.tour.dto.LocationBasedSearchParams
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourDetailParams
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourDetailResponse
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourResponse
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourSearchParams
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// 09.26 양현준
@Service
class TourService(
    private val tourApiClient: TourApiClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // 지역기반 관광정보 조회, areaBasedList2
    fun fetchTours(tourSearchParams: TourSearchParams): TourResponse {
        // request를 바탕으로 관광 정보 API 호출
        val tours = tourApiClient.fetchTourInfo(tourSearchParams)

        return tours
    }

    fun fetchLocationBasedTours(locationBasedSearchParams: LocationBasedSearchParams): TourResponse {
        return tourApiClient.fetchLocationBasedTours(locationBasedSearchParams)
    }

    fun fetchTourCommonDetail(params: TourDetailParams): TourDetailResponse {
        return tourApiClient.fetchTourCommonDetail(params)
    }
}
