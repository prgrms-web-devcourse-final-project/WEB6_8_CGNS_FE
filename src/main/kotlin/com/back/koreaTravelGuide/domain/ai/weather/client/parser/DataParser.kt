package com.back.koreaTravelGuide.domain.ai.weather.client.parser

import com.back.koreaTravelGuide.domain.ai.weather.dto.LandForecastData
import com.back.koreaTravelGuide.domain.ai.weather.dto.LandForecastInfo
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureData
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureInfo
import org.springframework.stereotype.Component

@Component
class DataParser {
    // JSON에서 값 추출 ("response.body.items.item[0].wfSv" 같은 경로로)
    fun extractJsonValue(
        jsonMap: Map<String, Any>,
        path: String,
    ): Any? {
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

    // 기온 데이터 JSON 파싱
    fun parseTemperatureDataFromJson(jsonResponse: Map<String, Any>): TemperatureData {
        val temperatureData = TemperatureData()

        for (day in 4..10) {
            val minTemp = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMin$day") as? Number)?.toInt()
            val maxTemp = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMax$day") as? Number)?.toInt()
            val minTempLow = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMin${day}Low") as? Number)?.toInt()
            val minTempHigh = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMin${day}High") as? Number)?.toInt()
            val maxTempLow = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMax${day}Low") as? Number)?.toInt()
            val maxTempHigh = (extractJsonValue(jsonResponse, "response.body.items.item[0].taMax${day}High") as? Number)?.toInt()

            if (minTemp != null || maxTemp != null) {
                val tempInfo =
                    TemperatureInfo(
                        minTemp = minTemp,
                        maxTemp = maxTemp,
                        minTempRange = if (minTempLow != null && minTempHigh != null) "$minTempLow~$minTempHigh℃" else null,
                        maxTempRange = if (maxTempLow != null && maxTempHigh != null) "$maxTempLow~$maxTempHigh℃" else null,
                    )

                temperatureData.setDay(day, tempInfo)
            }
        }

        return temperatureData
    }

    // 강수 확률 데이터 JSON 파싱
    fun parsePrecipitationDataFromJson(jsonResponse: Map<String, Any>): LandForecastData {
        val landForecastData = LandForecastData()

        for (day in 4..10) {
            if (day <= 7) {
                // 4~7일: 오전/오후 구분
                val amRain = (extractJsonValue(jsonResponse, "response.body.items.item[0].rnSt${day}Am") as? Number)?.toInt()
                val pmRain = (extractJsonValue(jsonResponse, "response.body.items.item[0].rnSt${day}Pm") as? Number)?.toInt()
                val amWeather = extractJsonValue(jsonResponse, "response.body.items.item[0].wf${day}Am") as? String
                val pmWeather = extractJsonValue(jsonResponse, "response.body.items.item[0].wf${day}Pm") as? String

                if (amRain != null || pmRain != null || !amWeather.isNullOrBlank() || !pmWeather.isNullOrBlank()) {
                    val precipInfo =
                        LandForecastInfo(
                            amRainPercent = amRain,
                            pmRainPercent = pmRain,
                            amWeather = amWeather,
                            pmWeather = pmWeather,
                        )

                    landForecastData.setDay(day, precipInfo)
                }
            } else {
                // 8~10일: 통합 (오전/오후 구분 없음)
                val rainPercent = (extractJsonValue(jsonResponse, "response.body.items.item[0].rnSt$day") as? Number)?.toInt()
                val weather = extractJsonValue(jsonResponse, "response.body.items.item[0].wf$day") as? String

                if (rainPercent != null || !weather.isNullOrBlank()) {
                    val precipInfo =
                        LandForecastInfo(
                            amRainPercent = rainPercent,
                            pmRainPercent = null,
                            amWeather = weather,
                            pmWeather = null,
                        )

                    landForecastData.setDay(day, precipInfo)
                }
            }
        }

        return landForecastData
    }
}
