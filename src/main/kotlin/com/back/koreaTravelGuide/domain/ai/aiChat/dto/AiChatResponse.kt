package com.back.koreaTravelGuide.domain.ai.aiChat.dto

import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatMessage

data class AiChatResponse(
    val userMessage: String,
    val aiMessage: String,
) {
    companion object {
        fun from(
            userMessage: AiChatMessage,
            aiMessage: AiChatMessage,
        ): AiChatResponse {
            return AiChatResponse(
                userMessage.content,
                aiMessage.content,
            )
        }
    }
}
