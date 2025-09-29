package com.back.koreaTravelGuide.domain.ai.weather.client

import com.back.koreaTravelGuide.KoreaTravelGuideApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 실제 기상청 API 상태를 확인하기 위한 통합 테스트.
 */
@SpringBootTest(classes = [KoreaTravelGuideApplication::class])
@ActiveProfiles("test")
class WeatherApiClientTest {
    @Autowired
    private lateinit var weatherApiClient: WeatherApiClient

    @Value("\${weather.api.key}")
    private lateinit var serviceKey: String

    private fun getCurrentBaseTime(): String {
        val now = LocalDateTime.now()
        val baseHour = if (now.hour >= 6) "0600" else "1800"
        val date = if (now.hour >= 6) now else now.minusDays(1)
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + baseHour
    }

    @DisplayName("fetchMidForecast - 실제 기상청 API 중기전망조회 (데이터 기대)")
    @Test
    fun fetchMidForecastTest() {
        assumeTrue(serviceKey.isNotBlank() && !serviceKey.contains("WEATHER_API_KEY")) {
            "API 키가 설정되지 않아 테스트를 건너뜁니다."
        }

        val regionId = "11B00000"
        val baseTime = getCurrentBaseTime()

        val result = weatherApiClient.fetchMidForecast(regionId, baseTime)

        assertThat(result).isNotNull()
    }

    @DisplayName("fetchTemperature - 실제 기상청 API 중기기온조회 (데이터 기대)")
    @Test
    fun fetchTemperatureTest() {
        assumeTrue(serviceKey.isNotBlank() && !serviceKey.contains("WEATHER_API_KEY")) {
            "API 키가 설정되지 않아 테스트를 건너뜁니다."
        }

        val regionId = "11B10101"
        val baseTime = getCurrentBaseTime()

        val result = weatherApiClient.fetchTemperature(regionId, baseTime)

        assertThat(result).isNotNull()
    }

    @DisplayName("fetchLandForecast - 실제 기상청 API 중기육상예보조회 (데이터 기대)")
    @Test
    fun fetchLandForecastTest() {
        assumeTrue(serviceKey.isNotBlank() && !serviceKey.contains("WEATHER_API_KEY")) {
            "API 키가 설정되지 않아 테스트를 건너뜁니다."
        }

        val regionId = "11B00000"
        val baseTime = getCurrentBaseTime()

        val result = weatherApiClient.fetchLandForecast(regionId, baseTime)

        assertThat(result).isNotNull()
    }
}
