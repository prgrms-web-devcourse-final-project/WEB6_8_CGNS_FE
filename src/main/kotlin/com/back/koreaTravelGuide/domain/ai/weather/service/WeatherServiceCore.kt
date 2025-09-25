package com.back.koreaTravelGuide.domain.ai.weather.service

// TODO: 날씨 정보 캐싱 서비스 - @Cacheable 어노테이션 기반 캐싱
import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.back.koreaTravelGuide.domain.weather.service.tools.Tools
import org.springframework.stereotype.Service

@Service
class WeatherServiceCore(
    val weatherService: WeatherService,
    val tools: Tools,
) {
    fun getWeatherForecast(
        location: String?,
        regionCode: String?,
        baseTime: String?,
    ): MidForecastDto? {
        val actualLocation = location ?: "서울"
        val actualRegionCode = regionCode ?: tools.getRegionCodeFromLocation(actualLocation)

        // baseTime 유효성 검사 - 06시 또는 18시만 허용
        val actualBaseTime = tools.validBaseTime(baseTime)

        return weatherService.fetchMidForecast(actualRegionCode, actualBaseTime)
    }

    fun getTemperatureAndLandForecast(
        location: String?,
        regionCode: String?,
        baseTime: String?,
    ): TemperatureAndLandForecastDto? {
        val actualLocation = location ?: "서울"
        val actualRegionCode = regionCode ?: tools.getRegionCodeFromLocation(actualLocation)

        // baseTime 유효성 검사 - 06시 또는 18시만 허용
        val actualBaseTime = tools.validBaseTime(baseTime)

        return weatherService.fetchTemperatureAndLandForecast(actualRegionCode, actualBaseTime)
    }
}
