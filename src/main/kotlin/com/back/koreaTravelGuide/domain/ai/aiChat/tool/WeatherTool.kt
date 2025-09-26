package com.back.koreaTravelGuide.domain.ai.aiChat.tool

// TODO: AI 날씨 도구 - Spring AI @Tool 어노테이션으로 AI가 호출할 수 있는 날씨 기능
import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.service.WeatherServiceCore
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class WeatherTool(
    private val weatherServiceCore: WeatherServiceCore,
) {
    @Tool(description = "현재 한국 시간을 조회합니다.")
    fun getCurrentTime(): String {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        return "현재 한국 표준시(KST): ${now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분"))}"
    }

    @Tool(description = "전국 중기전망 텍스트를 조회해 여행하기 좋은 지역 후보를 파악합니다. 먼저 호출하여 비교할 지역 코드를 추려 주세요.")
    fun queryMidTermNarrative(
        @ToolParam(description = "발표 시각 (YYYYMMDDHHMM). 미지정 시 최근 발표시각 사용.", required = false) baseTime: String?,
    ): List<MidForecastDto>? {
        return weatherServiceCore.getWeatherForecast(
            baseTime = baseTime,
        )
    }

    @Tool(description = "중기 기온과 강수 확률 지표를 지역별로 조회합니다. 첫 번째 툴에서 제안한 지역 코드로 비교 분석에 사용하세요.")
    fun queryMidTermAndLandForecastMetrics(
        @ToolParam(description = "지역 이름 (예: 서울, 인천)", required = true) location: String?,
        @ToolParam(description = "중기예보 regId (예: [\"11B10101\", \"11H20301\"]).", required = true) regionCode: String?,
//        @ToolParam(description = "중기예보 regId 배열 (예: [\"11B10101\", \"11H20301\"]).", required = true) regionCodes: List<String>,
        @ToolParam(description = "발표 시각 (YYYYMMDDHHMM). 미지정 시 최근 발표시각 사용.", required = false) baseTime: String?,
//        @ToolParam(description = "확인할 일 수 offset 목록 (4~10). 비워 두면 4~10일 모두 반환.", required = false) days: List<Int>?,
    ): TemperatureAndLandForecastDto? {
        return weatherServiceCore.getTemperatureAndLandForecast(
            location = location,
            regionCode = regionCode,
            baseTime = baseTime,
        )
    }

//    @Deprecated(
//        message = "AI 툴 분리 이후에는 queryMidTermNarrative/queryMidTermMetrics를 사용하세요.",
//        level = DeprecationLevel.WARNING,
//    )
//    fun getWeatherForecast(
//        location: String?,
//        regionCode: String?,
//        baseTime: String?,
//    ): WeatherResponse {
//        return weatherService.getWeatherForecast(
//            location = location,
//            regionCode = regionCode,
//            baseTime = baseTime,
//        )
//    }
}
