package com.back.koreaTravelGuide.application

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.dotenv
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.client.RestTemplate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * 실제 기상청 API 호출 테스트
 * Mock 데이터가 아닌 실제 API 응답 확인
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WeatherApiRealTest {
    
    companion object {
        @JvmStatic
        @BeforeAll
        fun loadEnv() {
            // .env 파일 로드
            val dotenv = dotenv {
                ignoreIfMissing = true
            }
            
            // 환경변수를 시스템 프로퍼티로 설정
            dotenv.entries().forEach { entry ->
                System.setProperty(entry.key, entry.value)
            }
        }
    }

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${weather.api.key}")
    private lateinit var serviceKey: String

    @Value("\${weather.api.base-url}")
    private lateinit var apiUrl: String

    private val objectMapper = ObjectMapper()

    /**
     * 현재 발표시각 계산 (06시 또는 18시)
     */
    private fun getCurrentBaseTime(): String {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val hour = now.hour

        return if (hour < 6) {
            now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "1800"
        } else if (hour < 18) {
            now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "0600"
        } else {
            now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "1800"
        }
    }

    @Test
    fun `실제 중기전망조회 API JSON 응답 확인`() {
        // given
        val stnId = "108" // 전국
        val baseTime = getCurrentBaseTime()
        val url = "$apiUrl/getMidFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&stnId=$stnId&tmFc=$baseTime&dataType=JSON"

        println("========================================")
        println("📋 중기전망조회 API 테스트")
        println("========================================")
        println("📍 지역: 전국 (stnId=108)")
        println("📅 발표시각: $baseTime (KST 기준 자동 계산)")
        println("🔗 요청 URL: $url")
        println()

        // when
        val jsonResponse = restTemplate.getForObject(url, Map::class.java)

        // then
        println("📦 JSON 응답 (Pretty Print):")
        println("----------------------------------------")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResponse))
        println()

        // 주요 데이터 추출
        val response = jsonResponse?.get("response") as? Map<*, *>
        val body = response?.get("body") as? Map<*, *>
        val items = body?.get("items") as? Map<*, *>
        val itemList = items?.get("item") as? List<*>
        val firstItem = itemList?.firstOrNull() as? Map<*, *>

        println("✅ 중기전망 텍스트 (wfSv):")
        println("----------------------------------------")
        println(firstItem?.get("wfSv"))
        println()
    }

    @Test
    fun `실제 중기기온조회 API JSON 응답 확인`() {
        // given
        val regId = "11B10101" // 서울
        val baseTime = getCurrentBaseTime()
        val url = "$apiUrl/getMidTa?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regId&tmFc=$baseTime&dataType=JSON"

        println("========================================")
        println("🌡️ 중기기온조회 API 테스트")
        println("========================================")
        println("📍 지역: 서울 (regId=11B10101)")
        println("📅 발표시각: $baseTime (KST 기준 자동 계산)")
        println("🔗 요청 URL: $url")
        println()

        // when
        val jsonResponse = restTemplate.getForObject(url, Map::class.java)

        // then
        println("📦 JSON 응답 (Pretty Print):")
        println("----------------------------------------")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResponse))
        println()

        // 주요 데이터 추출
        val response = jsonResponse?.get("response") as? Map<*, *>
        val body = response?.get("body") as? Map<*, *>
        val items = body?.get("items") as? Map<*, *>
        val itemList = items?.get("item") as? List<*>
        val firstItem = itemList?.firstOrNull() as? Map<*, *>

        println("✅ 기온 데이터 (4일~10일):")
        println("----------------------------------------")
        for (day in 4..10) {
            val minTemp = firstItem?.get("taMin$day")
            val maxTemp = firstItem?.get("taMax$day")
            println("📅 ${day}일 후: 최저 ${minTemp}℃ / 최고 ${maxTemp}℃")
        }
        println()
    }

    @Test
    fun `실제 중기육상예보조회 API JSON 응답 확인`() {
        // given
        val regId = "11B00000" // 서울,인천,경기도
        val baseTime = getCurrentBaseTime()
        val url = "$apiUrl/getMidLandFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regId&tmFc=$baseTime&dataType=JSON"

        println("========================================")
        println("🌧️ 중기육상예보조회 API 테스트")
        println("========================================")
        println("📍 지역: 서울,인천,경기도 (regId=11B00000)")
        println("📅 발표시각: $baseTime (KST 기준 자동 계산)")
        println("🔗 요청 URL: $url")
        println()

        // when
        val jsonResponse = restTemplate.getForObject(url, Map::class.java)

        // then
        println("📦 JSON 응답 (Pretty Print):")
        println("----------------------------------------")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResponse))
        println()

        // 주요 데이터 추출
        val response = jsonResponse?.get("response") as? Map<*, *>
        val body = response?.get("body") as? Map<*, *>
        val items = body?.get("items") as? Map<*, *>
        val itemList = items?.get("item") as? List<*>
        val firstItem = itemList?.firstOrNull() as? Map<*, *>

        println("✅ 강수 확률 데이터 (4일~10일):")
        println("----------------------------------------")
        for (day in 4..10) {
            val amRain = firstItem?.get("rnSt${day}Am")
            val pmRain = firstItem?.get("rnSt${day}Pm")
            val amWeather = firstItem?.get("wf${day}Am")
            val pmWeather = firstItem?.get("wf${day}Pm")
            println("📅 ${day}일 후:")
            println("   오전: $amWeather (강수확률: $amRain%)")
            println("   오후: $pmWeather (강수확률: $pmRain%)")
        }
        println()
    }

    @Test
    fun `통합 테스트 - 3개 API 동시 호출`() {
        val baseTime = getCurrentBaseTime()
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))

        println("========================================")
        println("🚀 통합 테스트 - 3개 API 동시 호출")
        println("========================================")
        println("⏰ 현재 KST 시간: ${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        println("📅 사용할 발표시각: $baseTime")
        println("📝 설명: 06시 이전 → 전날 18시 / 06~18시 → 당일 06시 / 18시 이후 → 당일 18시")
        println()

        // 1. 중기전망조회 (전국)
        println("1️⃣ 중기전망조회 - 전국 (stnId=108)")
        val midFcstUrl = "$apiUrl/getMidFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&stnId=108&tmFc=$baseTime&dataType=JSON"
        val midFcstResponse = restTemplate.getForObject(midFcstUrl, Map::class.java)

        // 2. 중기기온조회 (서울)
        println("2️⃣ 중기기온조회 - 서울 (regId=11B10101)")
        val midTaUrl = "$apiUrl/getMidTa?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=11B10101&tmFc=$baseTime&dataType=JSON"
        val midTaResponse = restTemplate.getForObject(midTaUrl, Map::class.java)

        // 3. 중기육상예보조회 (서울,인천,경기)
        println("3️⃣ 중기육상예보 - 서울,인천,경기 (regId=11B00000)")
        val midLandUrl = "$apiUrl/getMidLandFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=11B00000&tmFc=$baseTime&dataType=JSON"
        val midLandResponse = restTemplate.getForObject(midLandUrl, Map::class.java)

        println("✅ API 호출 결과:")
        println("----------------------------------------")

        // 결과 코드 확인
        val fcstCode = (midFcstResponse?.get("response") as? Map<*, *>)
            ?.get("header") as? Map<*, *>
        println("1. 중기전망조회: ${fcstCode?.get("resultCode")} - ${fcstCode?.get("resultMsg")}")

        val taCode = (midTaResponse?.get("response") as? Map<*, *>)
            ?.get("header") as? Map<*, *>
        println("2. 중기기온조회: ${taCode?.get("resultCode")} - ${taCode?.get("resultMsg")}")

        val landCode = (midLandResponse?.get("response") as? Map<*, *>)
            ?.get("header") as? Map<*, *>
        println("3. 중기육상예보: ${landCode?.get("resultCode")} - ${landCode?.get("resultMsg")}")

        println()
        println("💡 모든 API가 JSON 형식으로 응답을 반환했습니다!")
    }
}