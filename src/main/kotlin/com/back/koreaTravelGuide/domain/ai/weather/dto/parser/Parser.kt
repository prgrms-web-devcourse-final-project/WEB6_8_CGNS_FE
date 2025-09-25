package com.back.koreaTravelGuide.domain.weather.dto.parser

import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureDto
import org.springframework.stereotype.Component

@Component
class Parser {
    fun parseMidForecast(
        regionCode: String,
        baseTIme: String,
        rawText: String,
    ): MidForecastDto {
        // 섹션별로 분리 (○ 기호를 기준으로)
        val sections = rawText.split("○").filter { it.trim().isNotEmpty() }

        var precipitation = ""
        var temperature = ""
        var maritime = ""
        var variability = ""

        sections.forEach { section ->
            val trimmedSection = section.trim()
            when {
                trimmedSection.startsWith("(강수)") -> precipitation = "○ $trimmedSection"
                trimmedSection.startsWith("(기온)") -> temperature = "○ $trimmedSection"
                trimmedSection.startsWith("(해상)") -> {
                    // 해상 섹션에서 * (변동성) 부분을 제거
                    val maritimeSection =
                        if (trimmedSection.contains("*")) {
                            trimmedSection.substring(0, trimmedSection.indexOf("*")).trim()
                        } else {
                            trimmedSection
                        }
                    maritime = "○ $maritimeSection"
                }
            }
        }

        // 변동성 부분은 * 기호로 시작하는 부분을 찾아서 처리
        val variabilityIndex = rawText.indexOf("*")
        if (variabilityIndex != -1) {
            variability = rawText.substring(variabilityIndex).trim()
        }

        return MidForecastDto(
            regionCode = regionCode,
            baseTime = baseTIme,
            precipitation = precipitation,
            temperature = temperature,
            maritime = maritime,
            variability = variability,
        )
    }

    fun parseTemperatureAndLandForecast(
        temperatureDto: TemperatureDto,
        land,
    ): TemperatureAndLandForecastDto {
        return TemperatureAndLandForecastDto()
    }
    // 세 API의 데이터를 통합
//    private fun combineWeatherData(
//        midForecastText: String?,
//        temperatureData: TemperatureData?,
//        landForecastData: PrecipitationData?
//    ): CombinedWeatherData {
//
//        val summary = StringBuilder()
//
//        // 중기전망 텍스트 추가
//        if (!midForecastText.isNullOrBlank()) {
//            summary.append("📋 기상 전망:\n$midForecastText\n\n")
//        }
//
//        // 일별 상세 정보 구성
//        val details = WeatherDetails()
//
//        for (day in 4..10) {
//            val tempInfo = temperatureData?.getDay(day)
//            val precipInfo = landForecastData?.getDay(day)
//
//            if (tempInfo != null || precipInfo != null) {
//                val dayInfo = DayWeatherInfo(
//                    date = calculateDateFromDay(day),
//                    temperature = tempInfo,
//                    precipitation = precipInfo
//                )
//
//                details.setDay(day, dayInfo)
//
//                // 요약에 일별 정보 추가
//                summary.append("📅 ${day}일 후 (${dayInfo.date}):\n")
//
//                if (tempInfo != null) {
//                    summary.append("  🌡️ 기온: ")
//                    if (tempInfo.minTemp != null && tempInfo.maxTemp != null) {
//                        summary.append("${tempInfo.minTemp}℃~${tempInfo.maxTemp}℃")
//                    } else if (tempInfo.minTemp != null) {
//                        summary.append("최저 ${tempInfo.minTemp}℃")
//                    } else if (tempInfo.maxTemp != null) {
//                        summary.append("최고 ${tempInfo.maxTemp}℃")
//                    }
//                    summary.append("\n")
//                }
//
//                if (precipInfo != null) {
//                    if (precipInfo.amRainPercent != null || precipInfo.pmRainPercent != null) {
//                        summary.append("  🌧️ 강수확률: ")
//                        if (precipInfo.amRainPercent != null) {
//                            summary.append("오전 ${precipInfo.amRainPercent}% ")
//                        }
//                        if (precipInfo.pmRainPercent != null) {
//                            summary.append("오후 ${precipInfo.pmRainPercent}%")
//                        }
//                        summary.append("\n")
//                    }
//
//                    if (!precipInfo.amWeather.isNullOrBlank() || !precipInfo.pmWeather.isNullOrBlank()) {
//                        summary.append("  ☁️ 날씨: ")
//                        if (!precipInfo.amWeather.isNullOrBlank()) {
//                            summary.append("오전 ${precipInfo.amWeather} ")
//                        }
//                        if (!precipInfo.pmWeather.isNullOrBlank()) {
//                            summary.append("오후 ${precipInfo.pmWeather}")
//                        }
//                        summary.append("\n")
//                    }
//                }
//
//                summary.append("\n")
//            }
//        }
//
//        return CombinedWeatherData(
//            summary = summary.toString().trim(),
//            details = details
//        )
//    }

//    private fun calculateDateFromDay(daysAfter: Int): String {
//        // KST 기준으로 날짜 계산
//        val targetDate = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(daysAfter.toLong())
//        return targetDate.format(DateTimeFormatter.ofPattern("MM/dd"))
//    }
}
