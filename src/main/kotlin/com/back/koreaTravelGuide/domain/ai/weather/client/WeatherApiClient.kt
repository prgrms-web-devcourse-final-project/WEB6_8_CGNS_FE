package com.back.koreaTravelGuide.domain.ai.weather.client

// TODO: 기상청 API 클라이언트 - HTTP 요청으로 날씨 데이터 조회 및 JSON 파싱
import com.back.koreaTravelGuide.domain.ai.weather.client.parser.DataParser
import com.back.koreaTravelGuide.domain.ai.weather.client.tools.Tools
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureData
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class WeatherApiClient(
    private val restTemplate: RestTemplate,
    private val tools: Tools,
    private val dataParser: DataParser,
    @Value("\${weather.api.key}") private val serviceKey: String,
    @Value("\${weather.api.base-url}") private val apiUrl: String,
) {
    // 1. 중기전망조회 (getMidFcst) - 텍스트 기반 전망
    fun fetchMidForecast(
        regionId: String,
        baseTime: String,
    ): String? {
        val stnId = tools.getStnIdFromRegionCode(regionId)
        val url = "$apiUrl/getMidFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&stnId=$stnId&tmFc=$baseTime&dataType=JSON"

        println("🔮 중기전망조회 API 호출: $url")

        return try {
            @Suppress("UNCHECKED_CAST")
            val jsonResponse = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>
            println("📡 중기전망 JSON 응답 수신")

            jsonResponse?.let { response ->
                // API 오류 응답 체크
                val resultCode = dataParser.extractJsonValue(response, "response.header.resultCode") as? String
                if (resultCode == "03" || resultCode == "NO_DATA") {
                    println("⚠️ 기상청 API NO_DATA 오류 - 발표시각을 조정해야 할 수 있습니다")
                    return null
                }

                dataParser.extractJsonValue(response, "response.body.items.item[0].wfSv") as? String
            }
        } catch (e: Exception) {
            println("❌ 중기전망조회 JSON API 오류: ${e.message}")
            null
        }
    }

    // 2. 중기기온조회 (getMidTa) - 상세 기온 정보
    fun fetchTemperature(
        regionId: String,
        baseTime: String,
    ): TemperatureData? {
        val url = "$apiUrl/getMidTa?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regionId&tmFc=$baseTime&dataType=JSON"

        println("🌡️ 중기기온조회 API 호출: $url")

        return try {
            @Suppress("UNCHECKED_CAST")
            val jsonResponse = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>
            println("📡 중기기온 JSON 응답 수신")

            jsonResponse?.let { dataParser.parseTemperatureDataFromJson(it) } ?: TemperatureData()
        } catch (e: Exception) {
            println("❌ 중기기온조회 JSON API 오류: ${e.message}")
            TemperatureData()
        }
    }

    // 3. 중기육상예보조회 (getMidLandFcst) - 강수 확률
    fun fetchLandForecast(
        regionId: String,
        baseTime: String,
    ): PrecipitationData? {
        val url = "$apiUrl/getMidLandFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regionId&tmFc=$baseTime&dataType=JSON"

        println("🌧️ 중기육상예보조회 API 호출: $url")

        return try {
            @Suppress("UNCHECKED_CAST")
            val jsonResponse = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>
            println("📡 중기육상예보 JSON 응답 수신")

            jsonResponse?.let { dataParser.parsePrecipitationDataFromJson(it) } ?: PrecipitationData()
        } catch (e: Exception) {
            println("❌ 중기육상예보조회 JSON API 오류: ${e.message}")
            PrecipitationData()
        }
    }
}
