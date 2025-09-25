package com.back.koreaTravelGuide.domain.ai.weather.service

// TODO: 날씨 정보 캐싱 서비스 - @Cacheable 어노테이션 기반 캐싱
import com.back.koreaTravelGuide.domain.ai.weather.client.WeatherApiClient
import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.back.koreaTravelGuide.domain.weather.dto.parser.Parser
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class WeatherService(
    private val weatherApiClient: WeatherApiClient,
    private val parser: Parser,
) {
    @Cacheable("weatherMidFore", key = "#actualRegionCode + '_' + #actualBaseTime")
    fun fetchMidForecast(
        actualRegionCode: String,
        actualBaseTime: String,
    ): MidForecastDto? {
        val info = weatherApiClient.fetchMidForecast(actualRegionCode, actualBaseTime)

        if (info.isNullOrBlank()) return null

        return parser.parseMidForecast(actualRegionCode, actualBaseTime, info)
    }

    @Cacheable("weatherTempAndLandFore", key = "#actualRegionCode + '_' + #actualBaseTime")
    fun fetchTemperatureAndLandForecast(
        actualRegionCode: String,
        actualBaseTime: String,
    ): TemperatureAndLandForecastDto? {
        val tempInfo = weatherApiClient.fetchTemperature(actualRegionCode, actualBaseTime)
        val landInfo = weatherApiClient.fetchLandForecast(actualRegionCode, actualBaseTime)

        if (tempInfo == null || landInfo == null) return null

        return parser.parseTemperatureAndLandForecast(tempInfo, landInfo)
    }

    @CacheEvict(cacheNames = ["weatherMidFore", "weatherTempAndLandFore"], allEntries = true)
    @Scheduled(fixedRate = 43200000) // 12시간마다 (12 * 60 * 60 * 1000)
    fun clearWeatherCache() {
        println("🗑️ 날씨 캐시 자동 삭제 완료")
    }
}
