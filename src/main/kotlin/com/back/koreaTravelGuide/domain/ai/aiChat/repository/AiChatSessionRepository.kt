package com.back.koreaTravelGuide.domain.ai.aiChat.repository

import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AiChatSessionRepository : JpaRepository<AiChatSession, Long> {
    // 사용자별 세션 조회 (최신순)
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<AiChatSession>

    // 세션 아이디와 사용자 아이디로 세션 조회 (삭제 권한 체크)
    fun findByIdAndUserId(
        sessionId: Long,
        userId: Long,
    ): AiChatSession?
}
