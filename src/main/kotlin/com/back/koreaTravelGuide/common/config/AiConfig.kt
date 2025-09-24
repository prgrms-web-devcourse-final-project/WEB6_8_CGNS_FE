package com.back.koreaTravelGuide.common.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

/**
 * Spring AI 설정
 *
 * ChatClient와 RestTemplate 빈을 수동으로 생성하여 Spring AI 1.0.0-M6 호환성 보장
 */
@Configuration
class AiConfig {
    /**
     * RestTemplate 빈 생성
     * Spring AI가 OpenRouter/OpenAI API 호출에 사용
     */
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    /**
     * ChatClient 빈 생성
     * Spring AI 1.0.0-M6에서 자동 생성되지 않는 경우를 대비한 수동 설정
     */
    @Bean
    fun chatClient(chatModel: ChatModel): ChatClient {
        return ChatClient.builder(chatModel).build()
    }
}
