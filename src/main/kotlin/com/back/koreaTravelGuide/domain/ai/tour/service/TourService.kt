package com.back.koreaTravelGuide.domain.ai.tour.service

import com.back.koreaTravelGuide.domain.ai.tour.client.TourApiClient
import com.back.koreaTravelGuide.domain.ai.tour.dto.InternalData
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// 09.26 양현준
@Service
class TourService(
    private val tourApiClient: TourApiClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // 관광 정보 조회
    fun fetchTours(
        numOfRows: Int? = null,
        pageNo: Int? = null,
        contentTypeId: String? = null,
        areaCode: String? = null,
        sigunguCode: String? = null,
    ): List<TourResponse> {
        // InternalData 객체 생성, null 또는 비정상 값은 기본값으로 대체
        val request =
            InternalData(
                numOfRows = numOfRows?.takeIf { it > 0 } ?: InternalData.DEFAULT_ROWS,
                pageNo = pageNo?.takeIf { it > 0 } ?: InternalData.DEFAULT_PAGE,
                contentTypeId = contentTypeId?.ifBlank { null } ?: "",
                areaCode = areaCode?.ifBlank { null } ?: "",
                sigunguCode = sigunguCode?.ifBlank { null } ?: "",
            )

        // request를 바탕으로 관광 정보 API 호출
        val tours = tourApiClient.fetchTourInfo(request)

        // 관광 정보 결과 로깅
        if (tours.isEmpty()) {
            logger.info(
                "관광 정보 없음: params={} / {} {}",
                request.areaCode,
                request.sigunguCode,
                request.contentTypeId,
            )
        } else {
            logger.info("관광 정보 {}건 조회 성공", tours.size)
        }
        return tours
    }
}
