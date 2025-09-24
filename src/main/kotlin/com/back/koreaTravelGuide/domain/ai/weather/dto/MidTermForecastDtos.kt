package com.back.koreaTravelGuide.domain.ai.weather.dto

@Suppress("unused")
data class MidTermNarrativeResult(
    val baseTime: String,
    val primaryRegion: String,
    val summary: String,
    val candidateRegionCodes: List<String>,
)

@Suppress("unused")
data class MidTermMetricsResult(
    val requestedBaseTime: String?,
    val requestedDays: List<Int>,
    val regions: List<MidTermMetricsRegion>,
)

@Suppress("unused")
data class MidTermMetricsRegion(
    val regionCode: String,
    val location: String,
    val baseTime: String,
    val days: List<MidTermMetricsDay>,
)

@Suppress("unused")
data class MidTermMetricsDay(
    val dayOffset: Int,
    val date: String,
    val minTemp: Int?,
    val maxTemp: Int?,
    val amRainPercent: Int?,
    val pmRainPercent: Int?,
    val amWeather: String?,
    val pmWeather: String?,
)
