package com.back.koreaTravelGuide.domain.ai.tour.client

import com.back.koreaTravelGuide.application.KoreaTravelGuideApplication
import com.back.koreaTravelGuide.domain.ai.tour.dto.InternalData
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// 09.25 양현준
@ExtendWith(SpringExtension::class)
// 패키지 경로에서 메인 설정을 찾지 못하는 오류를 해결하기 위해 애플리케이션 클래스를 명시.
@SpringBootTest(classes = [KoreaTravelGuideApplication::class])
@ActiveProfiles("test")
class TourApiClientTest {
    @Autowired private lateinit var restTemplateBuilder: RestTemplateBuilder

    @Autowired private lateinit var objectMapper: ObjectMapper

    @Value("\${tour.api.key}")
    private lateinit var serviceKey: String

    @Value("\${tour.api.base-url}")
    private lateinit var apiUrl: String

    private lateinit var restTemplate: RestTemplate
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var tourApiClient: TourApiClient

    // 테스트마다 클라이언트와 Mock 서버를 새로 구성해 호출 상태를 초기화.
    @BeforeEach
    fun setUp() {
        restTemplate = restTemplateBuilder.build()
        mockServer = MockRestServiceServer.createServer(restTemplate)
        tourApiClient = TourApiClient(restTemplate, objectMapper, serviceKey, apiUrl)
    }

    // 첫 번째 관광 정보를 반환하는지.
    @DisplayName("TourApiClient - fetchTourInfo")
    @Test
    fun testReturnsFirstTourInfo() {
        val params = InternalData(numOfRows = 2, pageNo = 1, areaCode = "1", sigunguCode = "7")
        expectTourRequest(params, responseWithItems(sampleTourItem()))

        val result: TourResponse? = tourApiClient.fetchTourInfo(params)

        mockServer.verify()
        assertNotNull(result)
        assertEquals("2591792", result.contentId)
        assertEquals("개봉유수지 생태공원", result.title)
        assertEquals("7", result.sigunguCode)
    }

    // item 배열이 비어 있으면 null을 돌려주는지.
    @DisplayName("TourApiClient - fetchTourInfo")
    @Test
    fun testReturnsNullWhenItemsMissing() {
        val params = InternalData(numOfRows = 1, pageNo = 1, areaCode = "1", sigunguCode = "7")
        expectTourRequest(params, responseWithItems())

        val result = tourApiClient.fetchTourInfo(params)

        mockServer.verify()
        assertNull(result)
    }

    // 요청 URL과 응답 바디를 미리 세팅해 Mock 서버가 기대한 호출을 검증.
    private fun expectTourRequest(
        params: InternalData,
        responseBody: String,
    ) {
        mockServer.expect(requestTo(buildExpectedUrl(params)))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON))
    }

    // 실제 클라이언트가 조합하는 URL과 동일한 형태를 만들어 비교한다.
    private fun buildExpectedUrl(params: InternalData): String =
        UriComponentsBuilder.fromUri(URI.create(apiUrl))
            .path("/areaBasedList2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "KoreaTravelGuide")
            .queryParam("_type", "json")
            .queryParam("numOfRows", params.numOfRows)
            .queryParam("pageNo", params.pageNo)
            .queryParam("contentTypeId", params.contentTypeId)
            .queryParam("areaCode", params.areaCode)
            .queryParam("sigunguCode", params.sigunguCode)
            .build()
            .encode()
            .toUriString()

    // Jackson을 활용해 테스트 응답 JSON을 손쉽게 조립한다.
    private fun responseWithItems(vararg items: Map<String, String>): String {
        val response =
            mapOf(
                "response" to
                    mapOf(
                        "header" to mapOf("resultCode" to "0000", "resultMsg" to "OK"),
                        "body" to
                            mapOf(
                                "items" to mapOf("item" to items.toList()),
                            ),
                    ),
            )

        return objectMapper.writeValueAsString(response)
    }

    // 테스트용 샘플 관광지 정의한다.
    private fun sampleTourItem(): Map<String, String> =
        mapOf(
            "contentid" to "2591792",
            "contenttypeid" to "12",
            "createdtime" to "20190313221125",
            "modifiedtime" to "20250316162225",
            "title" to "개봉유수지 생태공원",
            "addr1" to "서울특별시 구로구 개봉동",
            "areacode" to "1",
            "firstimage" to "",
            "firstimage2" to "",
            "mapx" to "126.8632141714",
            "mapy" to "37.4924524597",
            "mlevel" to "6",
            "sigungucode" to "7",
            "lDongRegnCd" to "11",
            "lDongSignguCd" to "530",
        )

    // 테스트에서 RestTemplateBuilder 빈을 보장해 컨텍스트 로딩 실패 해결.
    @TestConfiguration
    class TestConfig {
        @Bean
        fun restTemplateBuilder(): RestTemplateBuilder = RestTemplateBuilder()
    }
}
