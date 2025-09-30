package com.back.koreaTravelGuide.domain.ai.weather.service

import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.service.tools.Tools
import org.springframework.stereotype.Service

@Service
class WeatherService(
    val weatherServiceCore: WeatherServiceCore,
    val tools: Tools,
) {
    fun getWeatherForecast(): List<MidForecastDto>? {
        val actualBaseTime = tools.getCurrentBaseTime()

        return weatherServiceCore.fetchMidForecast(actualBaseTime)
    }

    fun getTemperatureAndLandForecast(actualRegionCode: String): List<TemperatureAndLandForecastDto>? {
        val actualBaseTime = tools.getCurrentBaseTime()

        return weatherServiceCore.fetchTemperatureAndLandForecast(actualRegionCode, actualBaseTime)
    }
}
