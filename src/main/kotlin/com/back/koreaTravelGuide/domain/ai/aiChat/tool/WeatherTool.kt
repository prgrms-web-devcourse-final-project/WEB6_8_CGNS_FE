package com.back.koreaTravelGuide.domain.ai.aiChat.tool

import com.back.backend.BuildConfig
import com.back.koreaTravelGuide.common.logging.log
import com.back.koreaTravelGuide.domain.ai.weather.service.WeatherService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class WeatherTool(
    private val weatherService: WeatherService,
    private val objectMapper: ObjectMapper,
) {
    @Tool(description = "전국 중기예보를 조회합니다")
    fun getWeatherForecast(): String {
        log.info("🔧 [TOOL CALLED] getWeatherForecast")

        val forecasts = weatherService.getWeatherForecast()
        log.info("📦 [DATA] forecasts is null? ${forecasts == null}")
        log.info("📦 [DATA] forecasts 타입: ${forecasts?.javaClass?.name}")
        log.info("📦 [DATA] forecasts 내용: $forecasts")

        return try {
            val result = forecasts?.let { objectMapper.writeValueAsString(it) } ?: "중기예보 데이터를 가져올 수 없습니다."
            log.info("✅ [TOOL RESULT] getWeatherForecast - 결과: $result")
            result
        } catch (e: Exception) {
            log.error("❌ [TOOL ERROR] getWeatherForecast - 예외 발생: ${e.javaClass.name}", e)
            log.error("❌ [TOOL ERROR] 예외 메시지: ${e.message}")
            throw e
        }
    }

    @Tool(description = "특정 지역의 상세 기온 및 날씨 예보를 조회합니다")
    fun getRegionalWeatherDetails(
        @ToolParam(
            description = "지역 코드를 사용하세요. 사용 가능한 지역 코드: ${BuildConfig.REGION_CODES_DESCRIPTION}",
            required = true,
        )
        location: String,
    ): String {
        log.info("🔧 [TOOL CALLED] getRegionalWeatherDetails - location: $location")

        val forecasts = weatherService.getTemperatureAndLandForecast(location)

        return try {
            val result = forecasts?.let { objectMapper.writeValueAsString(it) } ?: "$location 지역의 상세 날씨 정보를 가져올 수 없습니다."
            log.info("✅ [TOOL RESULT] getRegionalWeatherDetails - 결과: $result")
            result
        } catch (e: Exception) {
            log.error("❌ [TOOL ERROR] getRegionalWeatherDetails - 예외 발생: ${e.javaClass.name}", e)
            log.error("❌ [TOOL ERROR] 예외 메시지: ${e.message}")
            log.error("❌ [TOOL ERROR] forecasts 타입: ${forecasts?.javaClass?.name}")
            log.error("❌ [TOOL ERROR] forecasts 내용: $forecasts")
            throw e
        }
    }
}
