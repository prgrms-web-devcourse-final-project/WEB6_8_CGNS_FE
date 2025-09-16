package com.back.koreaTravelGuide.domain.weather.service

// TODO: 날씨 정보 캐싱 서비스 - @Cacheable 어노테이션 기반 캐싱
import com.back.koreaTravelGuide.domain.weather.client.WeatherApiClient
import com.back.koreaTravelGuide.domain.weather.dto.*
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class WeatherService(
    private val weatherApiClient: WeatherApiClient
) {

    @Cacheable("weather", key = "#regionCode + '_' + #baseTime")
    fun getWeatherForecast(
        location: String?,
        regionCode: String?,
        baseTime: String?
    ): WeatherResponse {

        val actualLocation = location ?: "서울"
        val actualRegionCode = regionCode ?: getRegionCodeFromLocation(actualLocation)

        // baseTime 유효성 검사 - 06시 또는 18시만 허용
        val actualBaseTime = if (baseTime != null && (baseTime.endsWith("0600") || baseTime.endsWith("1800"))) {
            println("📌 제공된 발표시각 사용: $baseTime")
            baseTime
        } else {
            if (baseTime != null) {
                println("⚠️ 잘못된 발표시각 무시: $baseTime (06시 또는 18시만 유효)")
            }
            getCurrentBaseTime()
        }

        println("🌤️ 종합 날씨 정보 조회 시작 (캐시 미스)")
        println("📍 지역: $actualLocation")
        println("🔢 지역코드: $actualRegionCode")
        println("⏰ 발표시각: $actualBaseTime")

        return try {
            // 1. 중기전망조회 (텍스트 기반 전망)
            val midForecastResponse = weatherApiClient.fetchMidForecast(actualRegionCode, actualBaseTime)

            // 2. 중기기온조회 (상세 기온 정보)
            val temperatureResponse = weatherApiClient.fetchTemperature(actualRegionCode, actualBaseTime)

            // 3. 중기육상예보조회 (강수 확률)
            val landForecastResponse = weatherApiClient.fetchLandForecast(actualRegionCode, actualBaseTime)

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

    @CacheEvict("weather", allEntries = true)
    @Scheduled(fixedRate = 43200000) // 12시간마다 (12 * 60 * 60 * 1000)
    fun clearWeatherCache() {
        println("🗑️ 날씨 캐시 자동 삭제 완료")
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

        for (day in 4..10) {
            val tempInfo = temperatureData?.getDay(day)
            val precipInfo = landForecastData?.getDay(day)

            if (tempInfo != null || precipInfo != null) {
                val dayInfo = DayWeatherInfo(
                    date = calculateDateFromDay(day),
                    temperature = tempInfo,
                    precipitation = precipInfo
                )

                details.setDay(day, dayInfo)

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

    private fun calculateDateFromDay(daysAfter: Int): String {
        // KST 기준으로 날짜 계산
        val targetDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(daysAfter.toLong())
        return targetDate.format(DateTimeFormatter.ofPattern("MM/dd"))
    }

    private fun getCurrentBaseTime(): String {
        // 한국시간(KST) 기준으로 현재 시간 계산
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val hour = now.hour

        println("🕐 현재 KST 시간: ${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        return if (hour < 6) {
            // 06시 이전이면 전날 18시 발표
            val baseTime = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "1800"
            println("📅 사용할 발표시각: $baseTime (전날 18시)")
            baseTime
        } else if (hour < 18) {
            // 06시~18시 사이면 당일 06시 발표
            val baseTime = now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "0600"
            println("📅 사용할 발표시각: $baseTime (당일 06시)")
            baseTime
        } else {
            // 18시 이후면 당일 18시 발표
            val baseTime = now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "1800"
            println("📅 사용할 발표시각: $baseTime (당일 18시)")
            baseTime
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
}