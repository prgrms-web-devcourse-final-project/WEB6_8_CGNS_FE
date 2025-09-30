package com.back.koreaTravelGuide.domain.ai.aiChat.service

import com.back.backend.BuildConfig
import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatMessage
import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatSession
import com.back.koreaTravelGuide.domain.ai.aiChat.entity.SenderType
import com.back.koreaTravelGuide.domain.ai.aiChat.repository.AiChatMessageRepository
import com.back.koreaTravelGuide.domain.ai.aiChat.repository.AiChatSessionRepository
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.stereotype.Service

@Service
class AiChatService(
    private val aiChatMessageRepository: AiChatMessageRepository,
    private val aiChatSessionRepository: AiChatSessionRepository,
    private val chatClient: ChatClient,
) {
    fun getSessions(userId: Long): List<AiChatSession> {
        return aiChatSessionRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    fun createSession(userId: Long): AiChatSession {
        val newSession = AiChatSession(userId = userId, sessionTitle = "새로운 채팅방")
        return aiChatSessionRepository.save(newSession)
    }

    fun deleteSession(
        sessionId: Long,
        userId: Long,
    ) {
        val session = getSessionWithOwnershipCheck(sessionId, userId)

        aiChatSessionRepository.deleteById(sessionId)
    }

    fun getSessionMessages(
        sessionId: Long,
        userId: Long,
    ): List<AiChatMessage> {
        val session = getSessionWithOwnershipCheck(sessionId, userId)

        return aiChatMessageRepository.findByAiChatSessionIdOrderByCreatedAtAsc(sessionId)
    }

    fun sendMessage(
        sessionId: Long,
        userId: Long,
        message: String,
    ): Pair<AiChatMessage, AiChatMessage> {
        val session = getSessionWithOwnershipCheck(sessionId, userId)

        val userMessage =
            AiChatMessage(
                aiChatSession = session,
                senderType = SenderType.USER,
                content = message,
            )
        val savedUserMessage = aiChatMessageRepository.save(userMessage)

        if (aiChatMessageRepository.countByAiChatSessionId(sessionId) == 1L) {
            aiUpdateSessionTitle(session, message)
        }

        val response =
            try {
                chatClient.prompt()
                    .system(BuildConfig.KOREA_TRAVEL_GUIDE_SYSTEM)
                    .user(message)
                    .advisors { advisor ->
                        advisor.param(ChatMemory.CONVERSATION_ID, sessionId.toString())
                    }
                    .call()
                    .content() ?: BuildConfig.AI_ERROR_FALLBACK
            } catch (e: Exception) {
                BuildConfig.AI_ERROR_FALLBACK
            }

        val aiMessage =
            AiChatMessage(
                aiChatSession = session,
                senderType = SenderType.AI,
                content = response,
            )
        val savedAiMessage = aiChatMessageRepository.save(aiMessage)
        return Pair(savedUserMessage, savedAiMessage)
    }

    fun updateSessionTitle(
        sessionId: Long,
        userId: Long,
        newTitle: String,
    ): AiChatSession {
        val session = getSessionWithOwnershipCheck(sessionId, userId)
        session.sessionTitle = newTitle.trim().take(100)
        return aiChatSessionRepository.save(session)
    }

    /**
     * AiChatService 내 헬퍼 메서드들을 정의합니다.
     *
     * aiUpdateSessionTitle - AI를 사용하여 기본 채팅방 제목을 업데이트합니다.
     *
     * checkSessionOwnership - 세션 소유권을 확인합니다.
     */
    private fun aiUpdateSessionTitle(
        session: AiChatSession,
        userMessage: String,
    ) {
        val newTitle =
            try {
                chatClient.prompt()
                    .system("사용자의 채팅방 제목을 메시지를 요약해서 해당 사용자의 언어로 간결하게 만들어줘.")
                    .user(userMessage)
                    .call()
                    .content() ?: session.sessionTitle
            } catch (e: Exception) {
                session.sessionTitle
            }
        session.sessionTitle = newTitle.take(100)
        aiChatSessionRepository.save(session)
    }

    private fun getSessionWithOwnershipCheck(
        sessionId: Long,
        userId: Long,
    ): AiChatSession {
        return aiChatSessionRepository.findByIdAndUserId(sessionId, userId)
            ?: throw IllegalArgumentException("해당 채팅방이 없거나 접근 권한이 없습니다.")
    }
}
