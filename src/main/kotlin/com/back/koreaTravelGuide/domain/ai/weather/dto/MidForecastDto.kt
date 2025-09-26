package com.back.koreaTravelGuide.domain.ai.weather.dto

data class MidForecastDto(
    val regionCode: String?,
    val baseTime: String?,
    val precipitation: String?,
    val temperature: String?,
    val maritime: String?,
    val variability: String?,
)
