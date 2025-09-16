package com.back.controller

import com.back.tool.WeatherTool
import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class ChatController(
    private val chatClient: ChatClient,
    private val weatherTool: WeatherTool
) {

    @GetMapping("/ai")
    fun chat(@RequestParam(defaultValue = "서울 날씨 어때?") question: String): String {
        return try {
            chatClient.prompt()
                .user(question)
                .call()
                .content() ?: "응답을 받을 수 없습니다."
        } catch (e: Exception) {
            "오류 발생: ${e.message}"
        }
    }

    @GetMapping("/ai/stream")
    fun chatStream(@RequestParam(defaultValue = "서울 날씨 어때?") question: String): Flux<String> {
        return chatClient.prompt()
            .user(question)
            .stream()
            .content()
    }
    
    // 날씨 API 직접 테스트용 엔드포인트
    @GetMapping("/weather/test")
    fun testWeather(
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) regionCode: String?,
        @RequestParam(required = false) baseTime: String?
    ): Mono<WeatherTool.WeatherResponse> {
        val response = weatherTool.getWeatherForecast(
            location = location,
            regionCode = regionCode,
            baseTime = baseTime
        )
        return Mono.just(response)
    }
    
    // 지역별 날씨 간단 조회
    @GetMapping("/weather/simple")
    fun simpleWeather(@RequestParam(defaultValue = "서울") location: String): String {
        val response = weatherTool.getWeatherForecast(
            location = location,
            regionCode = null,
            baseTime = null
        )
        
        return """
            |지역: ${response.region}
            |지역코드: ${response.regionCode}
            |발표시각: ${response.baseTime}
            |
            |${response.forecast}
        """.trimMargin()
    }
    
    // 원시 XML 응답 확인용 엔드포인트
    @GetMapping("/weather/debug")
    fun debugWeatherApi(
        @RequestParam(defaultValue = "서울") location: String,
        @RequestParam(required = false) regionCode: String?,
        @RequestParam(required = false) baseTime: String?
    ): Map<String, Any?> {
        return try {
            println("🚀 디버그 API 호출 시작 - location: $location")
            val response = weatherTool.getWeatherForecast(
                location = location,
                regionCode = regionCode,
                baseTime = baseTime
            )
            
            mapOf(
                "success" to true,
                "location" to location,
                "regionCode" to (regionCode ?: "자동변환"),
                "baseTime" to (baseTime ?: "자동계산"),
                "response" to response,
                "hasData" to (response.details.day3 != null || response.details.day4 != null),
                "message" to "디버그 정보가 콘솔에 출력되었습니다."
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to (e.message ?: "알 수 없는 오류"),
                "location" to location,
                "message" to "오류 발생: ${e.message ?: "알 수 없는 오류"}"
            )
        }
    }
}