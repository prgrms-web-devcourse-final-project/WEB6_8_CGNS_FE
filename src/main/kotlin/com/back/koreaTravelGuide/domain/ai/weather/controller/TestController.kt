package com.back.koreaTravelGuide.domain.ai.weather.controller

import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.service.WeatherService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/weather")
class TestController(
    private val weatherService: WeatherService,
) {
    @GetMapping("/test1")
    fun test1(): List<MidForecastDto>? {
        return weatherService.getWeatherForecast()
    }

    @GetMapping("/test2")
    fun test2(): List<TemperatureAndLandForecastDto>? {
        return weatherService.getTemperatureAndLandForecast("11B10101")
    }
}
