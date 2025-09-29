package com.back.koreaTravelGuide.domain.ai.aiChat.tool

<<<<<<< HEAD
import com.back.koreaTravelGuide.domain.ai.weather.service.WeatherService
import com.back.koreaTravelGuide.domain.ai.weather.service.tools.Tools
=======
// TODO: AI 날씨 도구 - Spring AI @Tool 어노테이션으로 AI가 호출할 수 있는 날씨 기능
import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.service.WeatherService
>>>>>>> a51dd24 (feat(be): Add WeatherApiClientTest with ktlint compliance)
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class WeatherTool(
    private val weatherService: WeatherService,
<<<<<<< HEAD
    private val tools: Tools,
=======
>>>>>>> a51dd24 (feat(be): Add WeatherApiClientTest with ktlint compliance)
) {
    @Tool(description = "전국 중기예보를 조회합니다")
    fun getWeatherForecast(): String {
        val actualBaseTime = tools.validBaseTime(null)
        val forecasts = weatherService.fetchMidForecast(actualBaseTime)

        return forecasts?.toString() ?: "중기예보 데이터를 가져올 수 없습니다."
    }

<<<<<<< HEAD
    @Tool(description = "특정 지역의 상세 기온 및 날씨 예보를 조회합니다")
    fun getRegionalWeatherDetails(
        @ToolParam(description = "지역명 (예: 서울, 부산, 대전, 제주 등)", required = true)
        location: String,
    ): String {
        val regionCode = tools.getRegionCodeFromLocation(location)
        val actualBaseTime = tools.validBaseTime(null)
        val forecasts = weatherService.fetchTemperatureAndLandForecast(regionCode, actualBaseTime)

        return forecasts?.toString() ?: "$location 지역의 상세 날씨 정보를 가져올 수 없습니다."
=======
    @Tool(description = "전국 중기전망 텍스트를 조회해 여행하기 좋은 지역 후보를 파악합니다. 먼저 호출하여 비교할 지역 코드를 추려 주세요.")
    fun queryMidTermNarrative(
        @ToolParam(description = "발표 시각 (YYYYMMDDHHMM). 미지정 시 최근 발표시각 사용.", required = false) baseTime: String?,
    ): List<MidForecastDto>? {
        return weatherService.getWeatherForecast(
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
    ): List<TemperatureAndLandForecastDto>? {
        return weatherService.getTemperatureAndLandForecast(
            location = location,
            regionCode = regionCode,
            baseTime = baseTime,
        )
>>>>>>> a51dd24 (feat(be): Add WeatherApiClientTest with ktlint compliance)
    }
}
