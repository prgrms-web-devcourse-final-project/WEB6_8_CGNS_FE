package com.back.koreaTravelGuide.domain.ai.weather.dto

// TODO: 날씨 응답 DTO - 날씨 정보 및 상세 데이터 구조체
data class WeatherResponse(
    val region: String,
    val regionCode: String,
    val baseTime: String,
    val forecast: String,
    val details: WeatherDetails,
)

@Suppress("unused") // JSON 직렬화를 위해 필요
data class WeatherDetails(
    private val days: MutableMap<Int, DayWeatherInfo?> = mutableMapOf(),
) {
    var day4: DayWeatherInfo?
        get() = days[4]
        set(value) {
            days[4] = value
        }
    var day5: DayWeatherInfo?
        get() = days[5]
        set(value) {
            days[5] = value
        }
    var day6: DayWeatherInfo?
        get() = days[6]
        set(value) {
            days[6] = value
        }
    var day7: DayWeatherInfo?
        get() = days[7]
        set(value) {
            days[7] = value
        }
    var day8: DayWeatherInfo?
        get() = days[8]
        set(value) {
            days[8] = value
        }
    var day9: DayWeatherInfo?
        get() = days[9]
        set(value) {
            days[9] = value
        }
    var day10: DayWeatherInfo?
        get() = days[10]
        set(value) {
            days[10] = value
        }

    fun setDay(
        day: Int,
        info: DayWeatherInfo?,
    ) {
        days[day] = info
    }

    fun getDay(day: Int): DayWeatherInfo? = days[day]
}

data class DayWeatherInfo(
    val date: String,
    val temperature: TemperatureInfo?,
    val precipitation: PrecipitationInfo?,
)

data class TemperatureInfo(
    val minTemp: Int?,
    val maxTemp: Int?,
    val minTempRange: String?,
    val maxTempRange: String?,
)

data class PrecipitationInfo(
    val amRainPercent: Int?,
    val pmRainPercent: Int?,
    val amWeather: String?,
    val pmWeather: String?,
)
