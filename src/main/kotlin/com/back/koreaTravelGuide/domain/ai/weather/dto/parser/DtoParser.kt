package com.back.koreaTravelGuide.domain.weather.dto.parser

import com.back.koreaTravelGuide.domain.ai.weather.dto.LandForecastData
import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureData
import org.springframework.stereotype.Component

@Component
class DtoParser {
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
        regionCode: String,
        baseTime: String,
        temperatureData: TemperatureData,
        landForecastData: LandForecastData,
    ): List<TemperatureAndLandForecastDto> {
        val resultList = mutableListOf<TemperatureAndLandForecastDto>()

        for (i in 4..10) {
            val tempInfo = temperatureData.getDay(i)
            val landInfo = landForecastData.getDay(i)

            if (tempInfo == null || landInfo == null) {
                continue
            }

            val minTemp = tempInfo.minTemp
            val maxTemp = tempInfo.maxTemp
            val minTempRange = tempInfo.minTempRange
            val maxTempRange = tempInfo.maxTempRange

            val amRainPercent = landInfo.amRainPercent
            val pmRainPercent = landInfo.pmRainPercent
            val amWeather = landInfo.amWeather
            val pmWeather = landInfo.pmWeather

            // 각 날짜별로 필요한 처리를 수행합니다.
            val dto =
                TemperatureAndLandForecastDto(
                    regionCode = regionCode,
                    baseTime = baseTime,
                    minTemp = minTemp,
                    maxTemp = maxTemp,
                    minTempRange = minTempRange,
                    maxTempRange = maxTempRange,
                    amRainPercent = amRainPercent,
                    pmRainPercent = pmRainPercent,
                    amWeather = amWeather,
                    pmWeather = pmWeather,
                )

            resultList.add(dto)
        }

        return resultList
    }
}
