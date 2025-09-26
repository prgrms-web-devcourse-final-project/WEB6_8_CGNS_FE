package com.back.koreaTravelGuide.domain.ai.aiChat.repository

import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AiChatMessageRepository : JpaRepository<AiChatMessage, Long> {
    fun findByAiChatSessionIdOrderByCreatedAtAsc(sessionId: Long): List<AiChatMessage>

    fun countByAiChatSessionId(sessionId: Long): Long
}
