package com.back.koreaTravelGuide.infrastructure.config

import com.back.koreaTravelGuide.domain.chat.tool.WeatherTool
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Spring AI 설정
 *
 * ChatClient 빈 및 Tool 함수 등록
 * - Groq API 연동
 * - @Tool 어노테이션 함수 자동 등록
 *
 * 새로운 Tool 추가 방법:
 * 1. @Tool 어노테이션이 달린 클래스 생성
 * 2. defaultTools()에 추가
 *
 * ```kotlin
 * @Bean
 * fun chatClient(chatModel: ChatModel, weatherTool: WeatherTool, tourTool: TourTool): ChatClient {
 *     return ChatClient.builder(chatModel)
 *         .defaultTools(weatherTool, tourTool)  // 여기에 새 Tool 추가
 *         .build()
 * }
 * ```
 */
@Configuration
class AiConfig {

    /**
     * ChatClient 빈 생성
     * - 자동으로 application.yml의 Groq 설정 사용
     * - 등록된 모든 @Tool 함수들을 AI가 호출 가능하도록 설정
     */
    @Bean
    fun chatClient(chatModel: ChatModel, weatherTool: WeatherTool): ChatClient {
        println("🤖 ChatClient 초기화 중...")
        println("📏 WeatherTool 등록 완료")
        return ChatClient.builder(chatModel)
            .defaultTools(weatherTool)  // 새로운 Tool 여기에 추가
            .build()
    }
}