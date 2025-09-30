package com.back.koreaTravelGuide.domain.ai.weather.client

// TODO: 기상청 API 클라이언트 - HTTP 요청으로 날씨 데이터 조회 및 JSON 파싱
import com.back.koreaTravelGuide.common.logging.log
import com.back.koreaTravelGuide.domain.ai.weather.client.builder.UrlBuilder
import com.back.koreaTravelGuide.domain.ai.weather.client.parser.DataParser
import com.back.koreaTravelGuide.domain.ai.weather.client.tools.Tools
import com.back.koreaTravelGuide.domain.ai.weather.dto.LandForecastData
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureData
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class WeatherApiClient(
    private val restTemplate: RestTemplate,
    private val tools: Tools,
    private val dataParser: DataParser,
    private val builder: UrlBuilder,
) {
    // 1. 중기전망조회 (getMidFcst) - 텍스트 기반 전망
    fun fetchMidForecast(
        regionId: String,
        baseTime: String,
    ): String? {
        val stnId = tools.getStnIdFromRegionCode(regionId)
        val url = builder.buildMidFcstUrl(stnId, baseTime)

        return try {
            @Suppress("UNCHECKED_CAST")
            val jsonResponse = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>

            jsonResponse?.let { response ->
                // API 오류 응답 체크
                val resultCode = dataParser.extractJsonValue(response, "response.header.resultCode") as? String
                if (resultCode == "03" || resultCode == "NO_DATA") {
                    log.warn("기상청 API NO_DATA 오류 - 발표시각을 조정해야 할 수 있습니다")
                    return null
                }

                dataParser.extractJsonValue(response, "response.body.items.item[0].wfSv") as? String
            }
        } catch (e: Exception) {
            log.warn("중기전망조회 JSON API 오류: ${e.message}")
            null
        }
    }

    // 2. 중기기온조회 (getMidTa) - 상세 기온 정보
    fun fetchTemperature(
        regionId: String,
        baseTime: String,
    ): TemperatureData? {
        val url = builder.buildMidTaUrl(regionId, baseTime)

        return try {
            @Suppress("UNCHECKED_CAST")
            val jsonResponse = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>

            jsonResponse?.let { dataParser.parseTemperatureDataFromJson(it) } ?: TemperatureData()
        } catch (e: Exception) {
            log.warn("중기기온조회 JSON API 오류: ${e.message}")
            TemperatureData()
        }
    }

    // 3. 중기육상예보조회 (getMidLandFcst) - 강수 확률
    fun fetchLandForecast(
        regionId: String,
        baseTime: String,
    ): LandForecastData? {
        val url = builder.buildMidLandFcstUrl(regionId, baseTime)

        return try {
            @Suppress("UNCHECKED_CAST")
            val jsonResponse = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>

            jsonResponse?.let { dataParser.parsePrecipitationDataFromJson(it) } ?: LandForecastData()
        } catch (e: Exception) {
            log.warn("중기육상예보조회 JSON API 오류: ${e.message}")
            LandForecastData()
        }
    }
}
