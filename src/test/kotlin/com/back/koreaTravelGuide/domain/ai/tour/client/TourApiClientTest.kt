package com.back.koreaTravelGuide.domain.ai.tour.client

import com.back.koreaTravelGuide.KoreaTravelGuideApplication
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourParams
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertTrue

/**
 * 실제 관광청 API 상태를 확인하기 위한 통합 테스트.
 */
@SpringBootTest(classes = [KoreaTravelGuideApplication::class])
@ActiveProfiles("test")
class TourApiClientTest {
    @Autowired
    private lateinit var tourApiClient: TourApiClient

    @Value("\${tour.api.key}")
    private lateinit var serviceKey: String

    @DisplayName("fetchTourInfo - 실제 관광청 API 호출 (데이터 기대)")
    @Test
    fun fetchTourInfoTest() {
        val params =
            TourParams(
                contentTypeId = "12",
                areaCode = "1",
                sigunguCode = "1",
            )

        val result = tourApiClient.fetchTourInfo(params)

        println("실제 API 응답 아이템 수: ${result.items.size}")
        println("첫 번째 아이템: ${result.items.firstOrNull()}")

        assertTrue(result.items.isNotEmpty(), "실제 API 호출 결과가 비어 있습니다. 장애 여부를 확인하세요.")
    }

    @DisplayName("fetchTourInfo - 실제 관광청 API 장애 시 빈 결과 확인")
    @Test
    fun fetchTourInfoEmptyTest() {
        val params =
            TourParams(
                contentTypeId = "12",
                areaCode = "1",
                sigunguCode = "1",
            )

        val result = tourApiClient.fetchTourInfo(params)

        println("실제 API 응답 아이템 수: ${result.items.size}")
        println("첫 번째 아이템: ${result.items.firstOrNull()}")

        // 장애가 아닐 경우, 테스트를 스킵
        assumeTrue(result.items.isEmpty()) {
            "API가 정상 응답을 반환하고 있어 장애 시나리오 테스트를 건너뜁니다."
        }

        // 장애 상황일 시
        println("실제 API가 비어 있는 응답을 반환했습니다.")
        assertTrue(result.items.isEmpty())
    }
}
