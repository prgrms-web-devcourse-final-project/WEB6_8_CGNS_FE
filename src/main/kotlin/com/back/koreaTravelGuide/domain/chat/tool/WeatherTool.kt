package com.back.koreaTravelGuide.domain.chat.tool

// TODO: AI 날씨 도구 - Spring AI @Tool 어노테이션으로 AI가 호출할 수 있는 날씨 기능
import com.back.koreaTravelGuide.domain.weather.service.WeatherService
import com.back.koreaTravelGuide.domain.weather.dto.WeatherResponse
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class WeatherTool(
    private val weatherService: WeatherService
) {

    @Tool(description = "현재 한국 시간을 조회합니다.")
    fun getCurrentTime(): String {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        return "현재 한국 표준시(KST): ${now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"))}"
    }

    @Tool(description = "중기 날씨 예보를 조회합니다. 4일 후부터 10일 후까지의 날씨, 기온, 강수 확률 정보를 제공합니다. 3일 이내의 단기 예보는 제공하지 않습니다.")
    fun getWeatherForecast(
        @ToolParam(description = "지역 이름 (예: 서울, 부산, 대구 등)", required = false) location: String?,
        @ToolParam(description = "지역 코드 (예: 11B10101)", required = false) regionCode: String?,
        @ToolParam(description = "발표 시각 (YYYYMMDDHHMM)", required = false) baseTime: String?
    ): WeatherResponse {
        return weatherService.getWeatherForecast(
            location = location,
            regionCode = regionCode,
            baseTime = baseTime
        )
    }
}