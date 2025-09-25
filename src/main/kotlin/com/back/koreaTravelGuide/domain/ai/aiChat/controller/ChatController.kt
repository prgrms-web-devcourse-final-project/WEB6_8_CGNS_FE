package com.back.koreaTravelGuide.domain.ai.aiChat.controller

// TODO: 채팅 컨트롤러 - AI 채팅 API 및 SSE 스트리밍 엔드포인트 제공
import com.back.koreaTravelGuide.domain.ai.aiChat.tool.WeatherTool
import com.back.koreaTravelGuide.domain.ai.weather.dto.remove.WeatherResponse
import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.Disposable
import reactor.core.scheduler.Schedulers
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RestController
class ChatController(
    private val chatClient: ChatClient,
    private val weatherTool: WeatherTool,
    // @Value("\${chat.system-prompt}") private val systemPrompt: String,
) {
    @GetMapping("/ai")
    fun chat(
        @RequestParam(defaultValue = "서울 날씨 어때?") question: String,
    ): String {
        return try {
            chatClient.prompt()
                .system("한국 여행 전문가 AI입니다. 친근하고 정확한 정보를 제공하세요.")
                .user(question)
                .call()
                .content() ?: "응답을 받을 수 없습니다."
        } catch (e: Exception) {
            "오류 발생: ${e.message}"
        }
    }

    @GetMapping("/sse/ai", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun chatSse(
        @RequestParam q: String,
    ): SseEmitter {
        // 타임아웃 무제한(프록시/로드밸런서 idle 타임아웃은 별도 고려 필요)
        val emitter = SseEmitter(0L)

        // Spring AI: Flux<String> (토큰/청크 단위 문자열)
        val flux =
            chatClient
                .prompt() // 프롬프트 빌더 시작
                .system("한국 여행 전문가 AI입니다. 친근하고 정확한 정보를 제공하세요.") // 시스템 프롬프트 설정
                .user(q) // 사용자 메시지 설정
                .stream() // 스트리밍 모드 (Flux 반환)
                .content() // 텍스트만 추출(Flux<String>)

        // 구독 핸들 저장해서 커넥션 종료 시 해제
        lateinit var disposable: Disposable

        // 서블릿 스레드 점유 방지용 스케줄러 (I/O/네트워크는 boundedElastic 권장)
        disposable =
            flux
                .publishOn(Schedulers.boundedElastic())
                .doOnCancel {
                    // 클라이언트가 EventSource 닫으면 여기로 들어올 수 있음
                    emitter.complete()
                }
                .subscribe(
                    { chunk ->
                        // 각 토큰을 SSE 이벤트로 전송
                        // event: message \n data: <chunk> \n\n
                        try {
                            // 필요 시 커스텀 이벤트명
                            emitter.send(
                                SseEmitter.event()
                                    .name("message")
                                    // data 필드에 토큰 추가
                                    .data(chunk),
                            )
                        } catch (e: Exception) {
                            // 네트워크 끊김 등으로 전송 실패 시 구독 해제 및 종료
                            disposable.dispose()
                            emitter.completeWithError(e)
                        }
                    },
                    { e ->
                        // 에러 이벤트 전송 후 종료
                        try {
                            emitter.send(
                                SseEmitter.event()
                                    .name("error")
                                    .data("[ERROR] ${e.message}"),
                            )
                        } finally {
                            emitter.completeWithError(e)
                        }
                    },
                    {
                        // 완료 이벤트 전송 후 종료
                        try {
                            emitter.send(
                                SseEmitter.event()
                                    .name("done")
                                    .data("[DONE]"),
                            )
                        } finally {
                            emitter.complete()
                        }
                    },
                )

        // 서버/클라이언트 쪽에서 완료/타임아웃 시 정리
        emitter.onCompletion { disposable.dispose() }
        emitter.onTimeout {
            disposable.dispose()
            emitter.complete()
        }

        return emitter
    }

    // 날씨 API 직접 테스트용 엔드포인트
    @GetMapping("/weather/test")
    fun testWeather(
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) regionCode: String?,
        @RequestParam(required = false) baseTime: String?,
    ): WeatherResponse {
        return weatherTool.getWeatherForecast(
            location = location,
            regionCode = regionCode,
            baseTime = baseTime,
        )
    }

    // 지역별 날씨 간단 조회
    @GetMapping("/weather/simple")
    fun simpleWeather(
        @RequestParam(defaultValue = "서울") location: String,
    ): String {
        val response =
            weatherTool.getWeatherForecast(
                location = location,
                regionCode = null,
                baseTime = null,
            )

        return """
            |지역: ${response.region}
            |지역코드: ${response.regionCode}
            |발표시각: ${response.baseTime}
            |
            |${response.forecast}
            """.trimMargin()
    }

    // 현재 서버 시간 확인용 엔드포인트
    @GetMapping("/time/current")
    fun getCurrentTime(): Map<String, String> {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        return mapOf(
            "current_kst_time" to now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            "timezone" to "Asia/Seoul",
            "timestamp" to System.currentTimeMillis().toString(),
        )
    }

    // 원시 XML 응답 확인용 엔드포인트
    @GetMapping("/weather/debug")
    fun debugWeatherApi(
        @RequestParam(defaultValue = "서울") location: String,
        @RequestParam(required = false) regionCode: String?,
        @RequestParam(required = false) baseTime: String?,
    ): Map<String, Any?> {
        return try {
            println("🚀 디버그 API 호출 시작 - location: $location")
            val response =
                weatherTool.getWeatherForecast(
                    location = location,
                    regionCode = regionCode,
                    baseTime = baseTime,
                )

            mapOf(
                "success" to true,
                "location" to location,
                "regionCode" to (regionCode ?: "자동변환"),
                "baseTime" to (baseTime ?: "자동계산"),
                "response" to response,
                "hasData" to (response.details.day4 != null || response.details.day5 != null),
                "message" to "디버그 정보가 콘솔에 출력되었습니다.",
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to (e.message ?: "알 수 없는 오류"),
                "location" to location,
                "message" to "오류 발생: ${e.message ?: "알 수 없는 오류"}",
            )
        }
    }
}
