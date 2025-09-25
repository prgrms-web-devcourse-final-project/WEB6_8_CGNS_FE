package com.back.koreaTravelGuide.domain.ai.aiChat.repository

import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface AiChatMessageRepository : JpaRepository<AiChatMessage, Long> {
    fun findByAiChatSessionIdOrderByCreatedAtAsc(sessionId: Long): List<AiChatMessage>
}
