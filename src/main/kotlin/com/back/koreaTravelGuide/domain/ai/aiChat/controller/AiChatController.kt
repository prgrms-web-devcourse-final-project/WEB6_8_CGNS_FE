package com.back.koreaTravelGuide.domain.ai.aiChat.controller

import com.back.koreaTravelGuide.domain.ai.aiChat.dto.AiChatRequest
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.AiChatResponse
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.SessionMessagesResponse
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.SessionsResponse
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.UpdateSessionTitleRequest
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.UpdateSessionTitleResponse
import com.back.koreaTravelGuide.domain.ai.aiChat.service.AiChatService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/aichat")
class AiChatController(
    private val aiChatService: AiChatService,
) {
    @GetMapping("/sessions")
    fun getSessions(
        @RequestParam userId: Long,
    ): List<SessionsResponse> {
        return aiChatService.getSessions(userId).map {
            SessionsResponse(it.id!!, it.sessionTitle)
        }
    }

    @PostMapping("/sessions")
    fun createSession(
        @RequestParam userId: Long,
    ): SessionsResponse {
        val session = aiChatService.createSession(userId)
        return SessionsResponse(session.id!!, session.sessionTitle)
    }

    @DeleteMapping("/sessions/{sessionId}")
    fun deleteSession(
        @PathVariable sessionId: Long,
        @RequestParam userId: Long,
    ) {
        aiChatService.deleteSession(sessionId, userId)
    }

    @GetMapping("/sessions/{sessionId}/messages")
    fun getSessionMessages(
        @PathVariable sessionId: Long,
        @RequestParam userId: Long,
    ): List<SessionMessagesResponse> {
        val messages = aiChatService.getSessionMessages(sessionId, userId)
        return messages.map {
            SessionMessagesResponse(it.content, it.senderType)
        }
    }

    @PostMapping("/sessions/{sessionId}/messages")
    fun sendMessage(
        @PathVariable sessionId: Long,
        @RequestParam userId: Long,
        @RequestBody request: AiChatRequest,
    ): AiChatResponse {
        val (userMessage, aiMessage) = aiChatService.sendMessage(sessionId, userId, request.message)
        return AiChatResponse(
            userMessage = userMessage.content,
            aiMessage = aiMessage.content,
        )
    }

    @PatchMapping("/sessions/{sessionId}/title")
    fun updateSessionTitle(
        @PathVariable sessionId: Long,
        @RequestParam userId: Long,
        @RequestBody request: UpdateSessionTitleRequest,
    ): UpdateSessionTitleResponse {
        val updatedSession = aiChatService.updateSessionTitle(sessionId, userId, request.newTitle)
        return UpdateSessionTitleResponse(
            newTitle = updatedSession.sessionTitle,
        )
    }
}
