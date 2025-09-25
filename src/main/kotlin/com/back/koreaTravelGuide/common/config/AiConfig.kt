package com.back.koreaTravelGuide.common.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
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
     */
    @Bean
    fun chatClient(
        chatModel: ChatModel,
        chatMemory: ChatMemory,
    ): ChatClient {
        return ChatClient.builder(chatModel)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build()
    }

    /**
     * ChatMemory 빈 생성 (넉넉히 50개 메시지 유지, PostgreSQL 기반)
     */
    @Bean
    fun chatMemory(jdbcTemplate: JdbcTemplate): ChatMemory {
        val repository =
            JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .build()

        return MessageWindowChatMemory.builder()
            .maxMessages(50)
            .chatMemoryRepository(repository)
            .build()
    }
}
