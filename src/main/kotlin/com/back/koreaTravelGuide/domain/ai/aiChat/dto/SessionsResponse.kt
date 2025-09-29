package com.back.koreaTravelGuide.domain.ai.aiChat.dto

import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatSession

data class SessionsResponse(
    val sessionId: Long,
    val sessionTitle: String,
) {
    companion object {
        fun from(sessionResponse: AiChatSession) =
            SessionsResponse(
                sessionResponse.id!!,
                sessionResponse.sessionTitle,
            )
    }
}
