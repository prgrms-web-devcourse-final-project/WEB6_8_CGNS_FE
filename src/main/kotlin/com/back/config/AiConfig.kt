package com.back.config

import com.back.tool.WeatherTool
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {

    @Bean
    fun chatClient(chatModel: ChatModel, weatherTool: WeatherTool): ChatClient {
        println("📌 Registering WeatherTool with @Tool methods")

        return ChatClient.builder(chatModel)
            .defaultTools(weatherTool)  // @Tool 어노테이션이 있는 클래스 등록
            .build()
    }
}