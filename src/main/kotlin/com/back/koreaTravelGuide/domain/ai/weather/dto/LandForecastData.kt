package com.back.koreaTravelGuide.domain.ai.weather.dto

@Suppress("unused") // JSON 직렬화를 위해 필요
data class LandForecastData(
    private val days: MutableMap<Int, LandForecastInfo?> = mutableMapOf(),
) {
    fun setDay(
        day: Int,
        info: LandForecastInfo?,
    ) {
        days[day] = info
    }

    fun getDay(day: Int): LandForecastInfo? = days[day]

    var day4: LandForecastInfo? get() = days[4]
        set(value) {
            days[4] = value
        }
    var day5: LandForecastInfo? get() = days[5]
        set(value) {
            days[5] = value
        }
    var day6: LandForecastInfo? get() = days[6]
        set(value) {
            days[6] = value
        }
    var day7: LandForecastInfo? get() = days[7]
        set(value) {
            days[7] = value
        }
    var day8: LandForecastInfo? get() = days[8]
        set(value) {
            days[8] = value
        }
    var day9: LandForecastInfo? get() = days[9]
        set(value) {
            days[9] = value
        }
    var day10: LandForecastInfo? get() = days[10]
        set(value) {
            days[10] = value
        }
}

data class LandForecastInfo(
    val amRainPercent: Int?,
    val pmRainPercent: Int?,
    val amWeather: String?,
    val pmWeather: String?,
)
