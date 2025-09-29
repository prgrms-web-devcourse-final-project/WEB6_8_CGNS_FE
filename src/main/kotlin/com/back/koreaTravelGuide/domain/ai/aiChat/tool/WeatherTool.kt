package com.back.koreaTravelGuide.domain.ai.aiChat.tool

import com.back.koreaTravelGuide.domain.ai.weather.service.WeatherService
import com.back.koreaTravelGuide.domain.ai.weather.service.tools.Tools
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class WeatherTool(
    private val weatherService: WeatherService,
    private val tools: Tools,
) {
    @Tool(description = "전국 중기예보를 조회합니다")
    fun getWeatherForecast(): String {
        val actualBaseTime = tools.validBaseTime(null)
        val forecasts = weatherService.fetchMidForecast(actualBaseTime)

        return forecasts?.toString() ?: "중기예보 데이터를 가져올 수 없습니다."
    }

    @Tool(description = "특정 지역의 상세 기온 및 날씨 예보를 조회합니다")
    fun getRegionalWeatherDetails(
        @ToolParam(description = "지역명 (예: 서울, 부산, 대전, 제주 등)", required = true)
        location: String,
    ): String {
        val regionCode = tools.getRegionCodeFromLocation(location)
        val actualBaseTime = tools.validBaseTime(null)
        val forecasts = weatherService.fetchTemperatureAndLandForecast(regionCode, actualBaseTime)

        return forecasts?.toString() ?: "$location 지역의 상세 날씨 정보를 가져올 수 없습니다."
    }
}
