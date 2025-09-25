package com.back.koreaTravelGuide.domain.ai.aiChat.service

import com.back.koreaTravelGuide.common.constant.PromptConstant.AI_ERROR_FALLBACK
import com.back.koreaTravelGuide.common.constant.PromptConstant.KOREA_TRAVEL_GUIDE_SYSTEM
import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatMessage
import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatSession
import com.back.koreaTravelGuide.domain.ai.aiChat.entity.SenderType
import com.back.koreaTravelGuide.domain.ai.aiChat.repository.AiChatMessageRepository
import com.back.koreaTravelGuide.domain.ai.aiChat.repository.AiChatSessionRepository
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

@Service
class AiChatService(
    private val aiChatMessageRepository: AiChatMessageRepository,
    private val aiChatSessionRepository: AiChatSessionRepository,
    private val chatClient: ChatClient
) {
    fun getSessions(userId: Long): List<AiChatSession> {
        return aiChatSessionRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    fun createSession(userId: Long): AiChatSession {
        val newSession = AiChatSession(userId = userId)
        return aiChatSessionRepository.save(newSession)
    }

    fun deleteSession(sessionId: Long, userId: Long) {
        val session = aiChatSessionRepository.findByIdAndUserId(sessionId, userId)
            ?: throw IllegalArgumentException("해당 채팅방이 없거나 삭제 권한이 없습니다.")

        aiChatSessionRepository.deleteById(sessionId)
    }

    fun getSessionMessages(sessionId: Long, userId: Long): List<AiChatMessage> {
        val session = aiChatSessionRepository.findByIdAndUserId(sessionId, userId)
            ?: throw IllegalArgumentException("해당 채팅방이 없거나 접근 권한이 없습니다.")

        return aiChatMessageRepository.findByAiChatSessionIdOrderByCreatedAtAsc(sessionId)
    }

    fun sendMessage(sessionId: Long, userId: Long, message: String): Pair<AiChatMessage, AiChatMessage> {
        val session = aiChatSessionRepository.findByIdAndUserId(sessionId, userId)
            ?: throw IllegalArgumentException("해당 채팅방이 없거나 접근 권한이 없습니다.")

        val userMessage = AiChatMessage(
            aiChatSession = session,
            senderType = SenderType.USER,
            content = message
        )
        val savedUserMessage = aiChatMessageRepository.save(userMessage)

        val response = try {
            chatClient.prompt()
                .system(KOREA_TRAVEL_GUIDE_SYSTEM)
                .user(message)
                .call()
                .content() ?: AI_ERROR_FALLBACK
        } catch (e: Exception) {
            AI_ERROR_FALLBACK
        }

        val aiMessage = AiChatMessage(
            aiChatSession = session,
            senderType = SenderType.AI,
            content = response
        )
        val savedAiMessage = aiChatMessageRepository.save(aiMessage)
        return Pair(savedUserMessage, savedAiMessage)
    }
}
