package com.back.koreaTravelGuide.domain.ai.weather.service

import com.back.koreaTravelGuide.common.logging.log
import com.back.koreaTravelGuide.domain.ai.weather.client.WeatherApiClient
import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.back.koreaTravelGuide.domain.weather.dto.parser.DtoParser
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class WeatherServiceCore(
    private val weatherApiClient: WeatherApiClient,
    private val parser: DtoParser,
) {
    @Cacheable("weatherMidFore", key = "'전국_' + #actualBaseTime")
    fun fetchMidForecast(actualBaseTime: String): List<MidForecastDto>? {
        val prefixes = listOf("11B", "11D1", "11D2", "11C2", "11C1", "11F2", "11F1", "11H1", "11H2", "11G")
        val midForecastList = mutableListOf<MidForecastDto>()

        for (regionId in prefixes) {
            val info = weatherApiClient.fetchMidForecast(regionId, actualBaseTime)
            if (info.isNullOrBlank()) {
                log.warn("MidForecast Api error => regionId: $regionId")
                continue
            }

            val dto = parser.parseMidForecast(regionId, actualBaseTime, info)
            midForecastList.add(dto)
        }
        // 리스트가 비어있으면 null 반환 -> api 호출 실패 처리해야함.
        if (midForecastList.isEmpty()) return null
        return midForecastList
    }

    @Cacheable("weatherTempAndLandFore", key = "#actualRegionCode + '_' + #actualBaseTime")
    fun fetchTemperatureAndLandForecast(
        actualRegionCode: String,
        actualBaseTime: String,
    ): List<TemperatureAndLandForecastDto>? {
        val tempInfo = weatherApiClient.fetchTemperature(actualRegionCode, actualBaseTime)
        val landInfo = weatherApiClient.fetchLandForecast(actualRegionCode, actualBaseTime)

        // 둘 중 하나라도 null이면 null 반환 -> api 호출 실패 처리해야함.
        if (tempInfo == null || landInfo == null) {
            log.warn("Temp, Land Api error => actualRegionCode: $actualRegionCode")
            return null
        }

        return parser.parseTemperatureAndLandForecast(actualRegionCode, actualBaseTime, tempInfo, landInfo)
    }

    // 인스턴스가 여러개 있는 상황에서는 중복 삭제될 수 있음. 나중에 분산 락 고려
    @CacheEvict(cacheNames = ["weatherMidFore", "weatherTempAndLandFore"], allEntries = true)
    @Scheduled(fixedRate = 43200000) // 12시간마다 (12 * 60 * 60 * 1000)
    fun clearWeatherCache() {
        log.info("clearWeatherCache")
    }
}
