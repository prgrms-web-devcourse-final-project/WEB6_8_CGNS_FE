package com.back.koreaTravelGuide.domain.ai.weather.dto

data class TemperatureAndLandForecastDto(
    val regionCode: String?,
    val baseTime: String?,
    val minTemp: Int?,
    val maxTemp: Int?,
    val minTempRange: String?,
    val maxTempRange: String?,
    val amRainPercent: Int?,
    val pmRainPercent: Int?,
    val amWeather: String?,
    val pmWeather: String?,
)
