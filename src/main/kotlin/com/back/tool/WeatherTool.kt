package com.back.tool

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class WeatherTool(
    private val webClient: WebClient,
    @Value("\${weather.api.key}") private val serviceKey: String,
    @Value("\${weather.api.base-url}") private val apiUrl: String
) {

    @Tool(description = "중기 날씨 예보를 조회합니다. 3-10일 후의 날씨, 기온, 강수 확률 정보를 제공합니다.")
    fun getWeatherForecast(
        @ToolParam(description = "지역 이름 (예: 서울, 부산, 대구 등)", required = false) location: String?,
        @ToolParam(description = "지역 코드 (예: 11B10101)", required = false) regionCode: String?,
        @ToolParam(description = "발표 시각 (YYYYMMDDHHMM)", required = false) baseTime: String?
    ): WeatherResponse {
        
        val actualLocation = location ?: "서울"
        val actualRegionCode = regionCode ?: getRegionCodeFromLocation(actualLocation)
        val actualBaseTime = baseTime ?: getCurrentBaseTime()
        
        println("🌤️ 종합 날씨 정보 조회 시작")
        println("📍 지역: $actualLocation")
        println("🔢 지역코드: $actualRegionCode")
        println("⏰ 발표시각: $actualBaseTime")
        
        return try {
            // 1. 중기전망조회 (텍스트 기반 전망)
            val midForecastResponse = fetchMidForecast(actualRegionCode, actualBaseTime).block()
            
            // 2. 중기기온조회 (상세 기온 정보)
            val temperatureResponse = fetchTemperature(actualRegionCode, actualBaseTime).block()
            
            // 3. 중기육상예보조회 (강수 확률)
            val landForecastResponse = fetchLandForecast(actualRegionCode, actualBaseTime).block()
            
            // 데이터 병합 및 처리
            val combinedForecast = combineWeatherData(
                midForecastText = midForecastResponse,
                temperatureData = temperatureResponse,
                landForecastData = landForecastResponse
            )
            
            WeatherResponse(
                region = actualLocation,
                regionCode = actualRegionCode,
                baseTime = actualBaseTime,
                forecast = combinedForecast.summary,
                details = combinedForecast.details
            )
            
        } catch (e: Exception) {
            println("❌ 날씨 정보 조회 실패: ${e.message}")
            WeatherResponse(
                region = actualLocation,
                regionCode = actualRegionCode,
                baseTime = actualBaseTime,
                forecast = "날씨 정보를 가져올 수 없습니다: ${e.message}",
                details = WeatherDetails()
            )
        }
    }
    
    // 1. 중기전망조회 (getMidFcst) - 텍스트 기반 전망
    private fun fetchMidForecast(regionId: String, baseTime: String): Mono<String?> {
        val stnId = getStnIdFromRegionCode(regionId)
        val url = "$apiUrl/getMidFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&stnId=$stnId&tmFc=$baseTime&dataType=XML"
        
        println("🔮 중기전망조회 API 호출: $url")
        
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnNext { response ->
                println("📡 중기전망 응답 수신 (길이: ${response.length})")
            }
            .mapNotNull { xmlResponse ->
                val wfSvMatch = Regex("<wfSv><!\\[CDATA\\[(.*?)]]></wfSv>").find(xmlResponse)
                wfSvMatch?.groupValues?.get(1)?.trim()
            }
            .onErrorResume { 
                println("❌ 중기전망조회 API 오류: ${it.message}")
                Mono.empty()
            }
    }
    
    // 2. 중기기온조회 (getMidTa) - 상세 기온 정보
    private fun fetchTemperature(regionId: String, baseTime: String): Mono<TemperatureData?> {
        val url = "$apiUrl/getMidTa?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regionId&tmFc=$baseTime&dataType=XML"
        
        println("🌡️ 중기기온조회 API 호출: $url")
        
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnNext { response ->
                println("📡 중기기온 응답 수신 (길이: ${response.length})")
            }
            .map { xmlResponse ->
                parseTemperatureData(xmlResponse)
            }
            .onErrorResume { 
                println("❌ 중기기온조회 API 오류: ${it.message}")
                Mono.just(TemperatureData())
            }
    }
    
    // 3. 중기육상예보조회 (getMidLandFcst) - 강수 확률
    private fun fetchLandForecast(regionId: String, baseTime: String): Mono<PrecipitationData?> {
        val url = "$apiUrl/getMidLandFcst?serviceKey=$serviceKey&numOfRows=10&pageNo=1&regId=$regionId&tmFc=$baseTime&dataType=XML"
        
        println("🌧️ 중기육상예보조회 API 호출: $url")
        
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnNext { response ->
                println("📡 중기육상예보 응답 수신 (길이: ${response.length})")
            }
            .map { xmlResponse ->
                parsePrecipitationData(xmlResponse)
            }
            .onErrorResume { 
                println("❌ 중기육상예보조회 API 오류: ${it.message}")
                Mono.just(PrecipitationData())
            }
    }
    
    // 기온 데이터 파싱
    private fun parseTemperatureData(xmlResponse: String): TemperatureData {
        val temperatureData = TemperatureData()
        
        for (day in 3..10) {
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
                
                setTemperatureForDay(temperatureData, day, tempInfo)
            }
        }
        
        return temperatureData
    }
    
    // 강수 확률 데이터 파싱
    private fun parsePrecipitationData(xmlResponse: String): PrecipitationData {
        val precipitationData = PrecipitationData()
        
        for (day in 3..10) {
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
                
                setPrecipitationForDay(precipitationData, day, precipInfo)
            }
        }
        
        return precipitationData
    }
    
    // 세 API의 데이터를 통합
    private fun combineWeatherData(
        midForecastText: String?,
        temperatureData: TemperatureData?,
        landForecastData: PrecipitationData?
    ): CombinedWeatherData {
        
        val summary = StringBuilder()
        
        // 중기전망 텍스트 추가
        if (!midForecastText.isNullOrBlank()) {
            summary.append("📋 기상 전망:\n$midForecastText\n\n")
        }
        
        // 일별 상세 정보 구성
        val details = WeatherDetails()
        
        for (day in 3..10) {
            val tempInfo = getTemperatureForDay(temperatureData, day)
            val precipInfo = getPrecipitationForDay(landForecastData, day)
            
            if (tempInfo != null || precipInfo != null) {
                val dayInfo = DayWeatherInfo(
                    date = calculateDateFromDay(day),
                    temperature = tempInfo,
                    precipitation = precipInfo
                )
                
                setDayWeatherInfo(details, day, dayInfo)
                
                // 요약에 일별 정보 추가
                summary.append("📅 ${day}일 후 (${dayInfo.date}):\n")
                
                if (tempInfo != null) {
                    summary.append("  🌡️ 기온: ")
                    if (tempInfo.minTemp != null && tempInfo.maxTemp != null) {
                        summary.append("${tempInfo.minTemp}℃~${tempInfo.maxTemp}℃")
                    } else if (tempInfo.minTemp != null) {
                        summary.append("최저 ${tempInfo.minTemp}℃")
                    } else if (tempInfo.maxTemp != null) {
                        summary.append("최고 ${tempInfo.maxTemp}℃")
                    }
                    summary.append("\n")
                }
                
                if (precipInfo != null) {
                    if (precipInfo.amRainPercent != null || precipInfo.pmRainPercent != null) {
                        summary.append("  🌧️ 강수확률: ")
                        if (precipInfo.amRainPercent != null) {
                            summary.append("오전 ${precipInfo.amRainPercent}% ")
                        }
                        if (precipInfo.pmRainPercent != null) {
                            summary.append("오후 ${precipInfo.pmRainPercent}%")
                        }
                        summary.append("\n")
                    }
                    
                    if (!precipInfo.amWeather.isNullOrBlank() || !precipInfo.pmWeather.isNullOrBlank()) {
                        summary.append("  ☁️ 날씨: ")
                        if (!precipInfo.amWeather.isNullOrBlank()) {
                            summary.append("오전 ${precipInfo.amWeather} ")
                        }
                        if (!precipInfo.pmWeather.isNullOrBlank()) {
                            summary.append("오후 ${precipInfo.pmWeather}")
                        }
                        summary.append("\n")
                    }
                }
                
                summary.append("\n")
            }
        }
        
        return CombinedWeatherData(
            summary = summary.toString().trim(),
            details = details
        )
    }
    
    // 헬퍼 함수들 - Map 기반으로 간소화
    private fun setTemperatureForDay(temperatureData: TemperatureData, day: Int, tempInfo: TemperatureInfo) {
        temperatureData.setDay(day, tempInfo)
    }
    
    private fun setPrecipitationForDay(precipitationData: PrecipitationData, day: Int, precipInfo: PrecipitationInfo) {
        precipitationData.setDay(day, precipInfo)
    }
    
    private fun setDayWeatherInfo(details: WeatherDetails, day: Int, dayInfo: DayWeatherInfo) {
        details.setDay(day, dayInfo)
    }

    private fun getTemperatureForDay(temperatureData: TemperatureData?, day: Int): TemperatureInfo? {
        return temperatureData?.getDay(day)
    }
    
    private fun getPrecipitationForDay(precipitationData: PrecipitationData?, day: Int): PrecipitationInfo? {
        return precipitationData?.getDay(day)
    }
    
    private fun calculateDateFromDay(daysAfter: Int): String {
        val targetDate = LocalDateTime.now().plusDays(daysAfter.toLong())
        return targetDate.format(DateTimeFormatter.ofPattern("MM/dd"))
    }
    
    private fun extractXmlValue(xmlResponse: String, tagName: String): String? {
        val regex = Regex("<$tagName>(.*?)</$tagName>")
        return regex.find(xmlResponse)?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
    }
    
    private fun getCurrentBaseTime(): String {
        val now = LocalDateTime.now()
        val hour = now.hour
        
        return if (hour < 6) {
            now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "1800"
        } else if (hour < 18) {
            now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "0600"
        } else {
            now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "1800"
        }
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

    private fun getRegionCodeFromLocation(location: String): String {
        return REGION_MAP[location] ?: "11B10101"
    }

    companion object {
        private val REGION_MAP = mapOf(
            "서울" to "11B10101", "인천" to "11B20201", "수원" to "11B20601", "파주" to "11B20305",
            "이천" to "11B20612", "평택" to "11B20606", "춘천" to "11D10301", "원주" to "11D10401",
            "강릉" to "11D20501", "속초" to "11D20601", "대전" to "11C20401", "세종" to "11C20404",
            "청주" to "11C10301", "충주" to "11C10101", "전주" to "11F10201", "군산" to "11F10501",
            "광주" to "11F20501", "목포" to "11F20401", "여수" to "11F20801", "대구" to "11H10701",
            "안동" to "11H10501", "포항" to "11H10201", "부산" to "11H20201", "울산" to "11H20101",
            "창원" to "11H20301", "통영" to "11H20401", "제주" to "11G00201", "서귀포" to "11G00401"
        )
    }
    
    // 데이터 클래스들
    data class WeatherResponse(
        val region: String,
        val regionCode: String,
        val baseTime: String,
        val forecast: String,
        val details: WeatherDetails
    )
    
    @Suppress("unused") // JSON 직렬화를 위해 필요
    data class WeatherDetails(
        private val days: MutableMap<Int, DayWeatherInfo?> = mutableMapOf()
    ) {
        var day3: DayWeatherInfo? 
            get() = days[3]
            set(value) { days[3] = value }
        var day4: DayWeatherInfo?
            get() = days[4]
            set(value) { days[4] = value }
        var day5: DayWeatherInfo?
            get() = days[5]
            set(value) { days[5] = value }
        var day6: DayWeatherInfo?
            get() = days[6]
            set(value) { days[6] = value }
        var day7: DayWeatherInfo?
            get() = days[7]
            set(value) { days[7] = value }
        var day8: DayWeatherInfo?
            get() = days[8]
            set(value) { days[8] = value }
        var day9: DayWeatherInfo?
            get() = days[9]
            set(value) { days[9] = value }
        var day10: DayWeatherInfo?
            get() = days[10]
            set(value) { days[10] = value }
        
        fun setDay(day: Int, info: DayWeatherInfo?) {
            days[day] = info
        }
        
        fun getDay(day: Int): DayWeatherInfo? = days[day]
    }
    
    data class DayWeatherInfo(
        val date: String,
        val temperature: TemperatureInfo?,
        val precipitation: PrecipitationInfo?
    )
    
    data class TemperatureInfo(
        val minTemp: Int?,
        val maxTemp: Int?,
        val minTempRange: String?,
        val maxTempRange: String?
    )
    
    data class PrecipitationInfo(
        val amRainPercent: Int?,
        val pmRainPercent: Int?,
        val amWeather: String?,
        val pmWeather: String?
    )
    
    @Suppress("unused") // JSON 직렬화를 위해 필요
    data class TemperatureData(
        private val days: MutableMap<Int, TemperatureInfo?> = mutableMapOf()
    ) {
        fun setDay(day: Int, info: TemperatureInfo?) { days[day] = info }
        fun getDay(day: Int): TemperatureInfo? = days[day]
        var day3: TemperatureInfo? get() = days[3]; set(value) { days[3] = value }
        var day4: TemperatureInfo? get() = days[4]; set(value) { days[4] = value }
        var day5: TemperatureInfo? get() = days[5]; set(value) { days[5] = value }
        var day6: TemperatureInfo? get() = days[6]; set(value) { days[6] = value }
        var day7: TemperatureInfo? get() = days[7]; set(value) { days[7] = value }
        var day8: TemperatureInfo? get() = days[8]; set(value) { days[8] = value }
        var day9: TemperatureInfo? get() = days[9]; set(value) { days[9] = value }
        var day10: TemperatureInfo? get() = days[10]; set(value) { days[10] = value }
    }
    
    @Suppress("unused") // JSON 직렬화를 위해 필요
    data class PrecipitationData(
        private val days: MutableMap<Int, PrecipitationInfo?> = mutableMapOf()
    ) {
        fun setDay(day: Int, info: PrecipitationInfo?) { days[day] = info }
        fun getDay(day: Int): PrecipitationInfo? = days[day]
        var day3: PrecipitationInfo? get() = days[3]; set(value) { days[3] = value }
        var day4: PrecipitationInfo? get() = days[4]; set(value) { days[4] = value }
        var day5: PrecipitationInfo? get() = days[5]; set(value) { days[5] = value }
        var day6: PrecipitationInfo? get() = days[6]; set(value) { days[6] = value }
        var day7: PrecipitationInfo? get() = days[7]; set(value) { days[7] = value }
        var day8: PrecipitationInfo? get() = days[8]; set(value) { days[8] = value }
        var day9: PrecipitationInfo? get() = days[9]; set(value) { days[9] = value }
        var day10: PrecipitationInfo? get() = days[10]; set(value) { days[10] = value }
    }
    
    data class CombinedWeatherData(
        val summary: String,
        val details: WeatherDetails
    )
}