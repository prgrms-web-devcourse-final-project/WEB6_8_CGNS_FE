package com.back.koreaTravelGuide.domain.ai.weather.dto

// TODO: 날씨 내부 데이터 구조 - 기상청 API 응답 데이터 매핑용 내부 클래스들
@Suppress("unused") // JSON 직렬화를 위해 필요
data class TemperatureData(
    private val days: MutableMap<Int, TemperatureInfo?> = mutableMapOf(),
) {
    fun setDay(
        day: Int,
        info: TemperatureInfo?,
    ) {
        days[day] = info
    }

    fun getDay(day: Int): TemperatureInfo? = days[day]

    var day4: TemperatureInfo?
        get() = days[4]
        set(value) {
            days[4] = value
        }

    var day5: TemperatureInfo? get() = days[5]
        set(value) {
            days[5] = value
        }
    var day6: TemperatureInfo? get() = days[6]
        set(value) {
            days[6] = value
        }
    var day7: TemperatureInfo? get() = days[7]
        set(value) {
            days[7] = value
        }
    var day8: TemperatureInfo? get() = days[8]
        set(value) {
            days[8] = value
        }
    var day9: TemperatureInfo? get() = days[9]
        set(value) {
            days[9] = value
        }
    var day10: TemperatureInfo? get() = days[10]
        set(value) {
            days[10] = value
        }
}

data class TemperatureInfo(
    val minTemp: Int?,
    val maxTemp: Int?,
    val minTempRange: String?,
    val maxTempRange: String?,
)
