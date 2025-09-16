package com.back.koreaTravelGuide.domain.weather.client

// TODO: 기상청 API 클라이언트 - HTTP 요청으로 날씨 데이터 조회 및 XML 파싱
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
        val url = "$apiUrl/getMidFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&stnId=$stnId&tmFc=$baseTime&dataType=XML"

        println("🔮 중기전망조회 API 호출: $url")

        return try {
            val xmlResponse = restTemplate.getForObject(url, String::class.java)
            println("📡 중기전망 응답 수신 (길이: ${xmlResponse?.length ?: 0})")

            // API 오류 응답 체크
            xmlResponse?.let { response ->
                if (response.contains("<resultCode>03</resultCode>") || response.contains("NO_DATA")) {
                    println("⚠️ 기상청 API NO_DATA 오류 - 발표시각을 조정해야 할 수 있습니다")
                    return null
                }

                val wfSvMatch = Regex("<wfSv><!\\[CDATA\\[(.*?)]]></wfSv>").find(response)
                wfSvMatch?.groupValues?.get(1)?.trim()
            }
        } catch (e: Exception) {
            println("❌ 중기전망조회 API 오류: ${e.message}")
            null
        }
    }
    
    // 2. 중기기온조회 (getMidTa) - 상세 기온 정보
    fun fetchTemperature(regionId: String, baseTime: String): TemperatureData? {
        val url = "$apiUrl/getMidTa?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regionId&tmFc=$baseTime&dataType=XML"

        println("🌡️ 중기기온조회 API 호출: $url")

        return try {
            val xmlResponse = restTemplate.getForObject(url, String::class.java)
            println("📡 중기기온 응답 수신 (길이: ${xmlResponse?.length ?: 0})")

            xmlResponse?.let { parseTemperatureData(it) } ?: TemperatureData()
        } catch (e: Exception) {
            println("❌ 중기기온조회 API 오류: ${e.message}")
            TemperatureData()
        }
    }
    
    // 3. 중기육상예보조회 (getMidLandFcst) - 강수 확률
    fun fetchLandForecast(regionId: String, baseTime: String): PrecipitationData? {
        val url = "$apiUrl/getMidLandFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regionId&tmFc=$baseTime&dataType=XML"

        println("🌧️ 중기육상예보조회 API 호출: $url")

        return try {
            val xmlResponse = restTemplate.getForObject(url, String::class.java)
            println("📡 중기육상예보 응답 수신 (길이: ${xmlResponse?.length ?: 0})")

            xmlResponse?.let { parsePrecipitationData(it) } ?: PrecipitationData()
        } catch (e: Exception) {
            println("❌ 중기육상예보조회 API 오류: ${e.message}")
            PrecipitationData()
        }
    }
    
    // 기온 데이터 파싱
    private fun parseTemperatureData(xmlResponse: String): TemperatureData {
        val temperatureData = TemperatureData()
        
        for (day in 4..10) {
            val minTemp = extractXmlValue(xmlResponse, "taMin$day")?.toIntOrNull()
            val maxTemp = extractXmlValue(xmlResponse, "taMax$day")?.toIntOrNull()
            val minTempLow = extractXmlValue(xmlResponse, "taMin${day}Low")?.toIntOrNull()
            val minTempHigh = extractXmlValue(xmlResponse, "taMin${day}High")?.toIntOrNull()
            val maxTempLow = extractXmlValue(xmlResponse, "taMax${day}Low")?.toIntOrNull()
            val maxTempHigh = extractXmlValue(xmlResponse, "taMax${day}High")?.toIntOrNull()
            
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
    
    // 강수 확률 데이터 파싱
    private fun parsePrecipitationData(xmlResponse: String): PrecipitationData {
        val precipitationData = PrecipitationData()
        
        for (day in 4..10) {
            val amRain = extractXmlValue(xmlResponse, "rnSt${day}Am")?.toIntOrNull()
            val pmRain = extractXmlValue(xmlResponse, "rnSt${day}Pm")?.toIntOrNull()
            val amWeather = extractXmlValue(xmlResponse, "wf${day}Am")
            val pmWeather = extractXmlValue(xmlResponse, "wf${day}Pm")
            
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
    
    private fun extractXmlValue(xmlResponse: String, tagName: String): String? {
        val regex = Regex("<$tagName>(.*?)</$tagName>")
        return regex.find(xmlResponse)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
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