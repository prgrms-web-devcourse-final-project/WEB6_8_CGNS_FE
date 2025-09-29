package com.back.koreaTravelGuide.domain.ai.aiChat.dto

import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatMessage
import com.back.koreaTravelGuide.domain.ai.aiChat.entity.SenderType

data class SessionMessagesResponse(
    val content: String,
    val senderType: SenderType,
) {
    companion object {
        fun from(sessionMessage: AiChatMessage): SessionMessagesResponse {
            return SessionMessagesResponse(
                sessionMessage.content,
                sessionMessage.senderType,
            )
        }
    }
}
