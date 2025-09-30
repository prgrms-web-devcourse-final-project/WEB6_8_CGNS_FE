package com.back.koreaTravelGuide.domain.ai.aiChat.tool

import com.back.backend.BuildConfig
import com.back.koreaTravelGuide.domain.ai.weather.service.WeatherService
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class WeatherTool(
    private val weatherService: WeatherService,
) {
    @Tool(description = "전국 중기예보를 조회합니다")
    fun getWeatherForecast(): String {
        val forecasts = weatherService.getWeatherForecast()

        return forecasts?.toString() ?: "중기예보 데이터를 가져올 수 없습니다."
    }

    @Tool(description = "특정 지역의 상세 기온 및 날씨 예보를 조회합니다")
    fun getRegionalWeatherDetails(
        @ToolParam(description = BuildConfig.REGION_CODES_DESCRIPTION, required = true)
        location: String,
    ): String {
        val forecasts = weatherService.getTemperatureAndLandForecast(location)

        return forecasts?.toString() ?: "$location 지역의 상세 날씨 정보를 가져올 수 없습니다."
    }
}
