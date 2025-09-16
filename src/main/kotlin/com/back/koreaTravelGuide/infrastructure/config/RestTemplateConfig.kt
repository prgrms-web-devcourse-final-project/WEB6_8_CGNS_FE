package com.back.koreaTravelGuide.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import java.time.Duration

/**
 * RestTemplate 설정
 *
 * HTTP 클라이언트 빈 설정
 * - 외부 API 호출용 (기상청, 관광청 등)
 * - 연결 및 읽기 타임아웃 설정
 *
 * 사용법:
 * ```kotlin
 * @Component
 * class YourApiClient(
 *     private val restTemplate: RestTemplate  // 자동 주입
 * ) {
 *     fun callApi(): String? {
 *         return restTemplate.getForObject("https://api.example.com", String::class.java)
 *     }
 * }
 * ```
 */
@Configuration
class RestTemplateConfig {

    /**
     * 기본 RestTemplate 빈
     * - 연결 타임아웃: 10초 (서버와 연결 시도)
     * - 읽기 타임아웃: 30초 (응답 대기)
     */
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build()
    }
}