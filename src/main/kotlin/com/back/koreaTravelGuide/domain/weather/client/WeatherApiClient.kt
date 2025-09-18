package com.back.koreaTravelGuide.domain.weather.client

// TODO: 기상청 API 클라이언트 - HTTP 요청으로 날씨 데이터 조회 및 JSON 파싱
import com.back.koreaTravelGuide.domain.weather.dto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class WeatherApiClient(
    private val restTemplate: RestTemplate,
    @Value("\${weather.api.key}") private val serviceKey: String,
    @Value("\${weather.api.base-url}") private val apiUrl: String
) {
    
    // 1. 중기전망조회 (getMidFcst) - 텍스트 기반 전망
    fun fetchMidForecast(regionId: String, baseTime: String): String? {
        val stnId = getStnIdFromRegionCode(regionId)
        val url = "$apiUrl/getMidFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&stnId=$stnId&tmFc=$baseTime&dataType=JSON"

        println("🔮 중기전망조회 API 호출: $url")

        return try {
            @Suppress("UNCHECKED_CAST")
            val jsonResponse = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>
            println("📡 중기전망 JSON 응답 수신")

            jsonResponse?.let { response ->
                // API 오류 응답 체크
                val resultCode = extractJsonValue(response, "response.header.resultCode") as? String
                if (resultCode == "03" || resultCode == "NO_DATA") {
                    println("⚠️ 기상청 API NO_DATA 오류 - 발표시각을 조정해야 할 수 있습니다")
                    return null
                }

                extractJsonValue(response, "response.body.items.item[0].wfSv") as? String
            }
        } catch (e: Exception) {
            println("❌ 중기전망조회 JSON API 오류: ${e.message}")
            null
        }
    }
    
    // 2. 중기기온조회 (getMidTa) - 상세 기온 정보
    fun fetchTemperature(regionId: String, baseTime: String): TemperatureData? {
        val url = "$apiUrl/getMidTa?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regionId&tmFc=$baseTime&dataType=JSON"

        println("🌡️ 중기기온조회 API 호출: $url")

        return try {
            @Suppress("UNCHECKED_CAST")
            val jsonResponse = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>
            println("📡 중기기온 JSON 응답 수신")

            jsonResponse?.let { parseTemperatureDataFromJson(it) } ?: TemperatureData()
        } catch (e: Exception) {
            println("❌ 중기기온조회 JSON API 오류: ${e.message}")
            TemperatureData()
        }
    }
    
    // 3. 중기육상예보조회 (getMidLandFcst) - 강수 확률
    fun fetchLandForecast(regionId: String, baseTime: String): PrecipitationData? {
        val url = "$apiUrl/getMidLandFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regionId&tmFc=$baseTime&dataType=JSON"

        println("🌧️ 중기육상예보조회 API 호출: $url")

        return try {
            @Suppress("UNCHECKED_CAST")
            val jsonResponse = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any>
            println("📡 중기육상예보 JSON 응답 수신")

            jsonResponse?.let { parsePrecipitationDataFromJson(it) } ?: PrecipitationData()
        } catch (e: Exception) {
            println("❌ 중기육상예보조회 JSON API 오류: ${e.message}")
            PrecipitationData()
        }
    }
    
    // 기온 데이터 JSON 파싱
    private fun parseTemperatureDataFromJson(jsonResponse: Map<String, Any>): TemperatureData {
        val temperatureData = TemperatureData()

        for (day in 4..10) {
            val minTemp = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMin$day") as? Number)?.toInt()
            val maxTemp = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMax$day") as? Number)?.toInt()
            val minTempLow = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMin${day}Low") as? Number)?.toInt()
            val minTempHigh = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMin${day}High") as? Number)?.toInt()
            val maxTempLow = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMax${day}Low") as? Number)?.toInt()
            val maxTempHigh = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMax${day}High") as? Number)?.toInt()

            if (minTemp != null || maxTemp != null) {
                val tempInfo = TemperatureInfo(
                    minTemp = minTemp,
                    maxTemp = maxTemp,
                    minTempRange = if (minTempLow != null && minTempHigh != null) "$minTempLow~$minTempHigh℃" else null,
                    maxTempRange = if (maxTempLow != null && maxTempHigh != null) "$maxTempLow~$maxTempHigh℃" else null
                )

                temperatureData.setDay(day, tempInfo)
            }
        }

        return temperatureData
    }
    
    // 강수 확률 데이터 JSON 파싱
    private fun parsePrecipitationDataFromJson(jsonResponse: Map<String, Any>): PrecipitationData {
        val precipitationData = PrecipitationData()

        for (day in 4..10) {
            val amRain = (extractJsonValue(jsonResponse, "response.body.items.item[0].rnSt${day}Am") as? Number)?.toInt()
            val pmRain = (extractJsonValue(jsonResponse, "response.body.items.item[0].rnSt${day}Pm") as? Number)?.toInt()
            val amWeather = extractJsonValue(jsonResponse, "response.body.items.item[0].wf${day}Am") as? String
            val pmWeather = extractJsonValue(jsonResponse, "response.body.items.item[0].wf${day}Pm") as? String

            if (amRain != null || pmRain != null || !amWeather.isNullOrBlank() || !pmWeather.isNullOrBlank()) {
                val precipInfo = PrecipitationInfo(
                    amRainPercent = amRain,
                    pmRainPercent = pmRain,
                    amWeather = amWeather,
                    pmWeather = pmWeather
                )

                precipitationData.setDay(day, precipInfo)
            }
        }

        return precipitationData
    }
    
    // JSON에서 값 추출 ("response.body.items.item[0].wfSv" 같은 경로로)
    private fun extractJsonValue(jsonMap: Map<String, Any>, path: String): Any? {
        var current: Any? = jsonMap
        val parts = path.split(".")

        for (part in parts) {
            when {
                current == null -> return null
                part.contains("[") && part.contains("]") -> {
                    // 배열 인덱스 처리 (item[0] 같은 경우)
                    val arrayName = part.substringBefore("[")
                    val index = part.substringAfter("[").substringBefore("]").toIntOrNull() ?: 0

                    current = (current as? Map<*, *>)?.get(arrayName)
                    current = (current as? List<*>)?.getOrNull(index)
                }
                else -> {
                    current = (current as? Map<*, *>)?.get(part)
                }
            }
        }

        return current
    }
    
    private fun getStnIdFromRegionCode(regionCode: String): String {
        return when {
            regionCode.startsWith("11B") -> "109" // 서울,인천,경기도
            regionCode.startsWith("11D1") -> "105" // 강원도영서
            regionCode.startsWith("11D2") -> "105" // 강원도영동
            regionCode.startsWith("11C2") -> "133" // 대전,세종,충청남도
            regionCode.startsWith("11C1") -> "131" // 충청북도
            regionCode.startsWith("11F2") -> "156" // 광주,전라남도
            regionCode.startsWith("11F1") -> "146" // 전북자치도
            regionCode.startsWith("11H1") -> "143" // 대구,경상북도
            regionCode.startsWith("11H2") -> "159" // 부산,울산,경상남도
            regionCode.startsWith("11G") -> "184" // 제주도
            else -> "108" // 전국
        }
    }
}