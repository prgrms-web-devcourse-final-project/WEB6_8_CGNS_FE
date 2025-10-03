package com.back.koreaTravelGuide.domain.ai.aiChat.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.common.security.getUserId
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.AiChatRequest
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.AiChatResponse
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.SessionMessagesResponse
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.SessionsResponse
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.UpdateSessionTitleRequest
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.UpdateSessionTitleResponse
import com.back.koreaTravelGuide.domain.ai.aiChat.service.AiChatService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/aichat")
class AiChatController(
    private val aiChatService: AiChatService,
) {
    @GetMapping("/sessions")
    fun getSessions(authentication: Authentication?): ResponseEntity<ApiResponse<List<SessionsResponse>>> {
        val userId = authentication?.getUserId() ?: 1L // dev 모드: 기본 userId=1
        val sessions =
            aiChatService.getSessions(userId).map {
                SessionsResponse.from(it)
            }
        return ResponseEntity.ok(ApiResponse("채팅방 목록을 성공적으로 조회했습니다.", sessions))
    }

    @PostMapping("/sessions")
    fun createSession(authentication: Authentication?): ResponseEntity<ApiResponse<SessionsResponse>> {
        val userId = authentication?.getUserId() ?: 1L // dev 모드: 기본 userId=1
        val session = aiChatService.createSession(userId)
        val response = SessionsResponse.from(session)
        return ResponseEntity.ok(ApiResponse("채팅방이 성공적으로 생성되었습니다.", response))
    }

    @DeleteMapping("/sessions/{sessionId}")
    fun deleteSession(
        @PathVariable sessionId: Long,
        authentication: Authentication?,
    ): ResponseEntity<ApiResponse<Unit>> {
        val userId = authentication?.getUserId() ?: 1L // dev 모드: 기본 userId=1
        aiChatService.deleteSession(sessionId, userId)
        return ResponseEntity.ok(ApiResponse("채팅방이 성공적으로 삭제되었습니다."))
    }

    @GetMapping("/sessions/{sessionId}/messages")
    fun getSessionMessages(
        @PathVariable sessionId: Long,
        authentication: Authentication?,
    ): ResponseEntity<ApiResponse<List<SessionMessagesResponse>>> {
        val userId = authentication?.getUserId() ?: 1L // dev 모드: 기본 userId=1
        val messages = aiChatService.getSessionMessages(sessionId, userId)
        val response =
            messages.map {
                SessionMessagesResponse(it.content, it.senderType)
            }
        return ResponseEntity.ok(ApiResponse("채팅 메시지를 성공적으로 조회했습니다.", response))
    }

    @PostMapping("/sessions/{sessionId}/messages")
    fun sendMessage(
        @PathVariable sessionId: Long,
        authentication: Authentication?,
        @RequestBody request: AiChatRequest,
    ): ResponseEntity<ApiResponse<AiChatResponse>> {
        val userId = authentication?.getUserId() ?: 1L // dev 모드: 기본 userId=1
        val (userMessage, aiMessage) = aiChatService.sendMessage(sessionId, userId, request.message)
        val response =
            AiChatResponse(
                userMessage = userMessage.content,
                aiMessage = aiMessage.content,
            )
        return ResponseEntity.ok(ApiResponse("메시지가 성공적으로 전송되었습니다.", response))
    }

    @PatchMapping("/sessions/{sessionId}/title")
    fun updateSessionTitle(
        @PathVariable sessionId: Long,
        authentication: Authentication?,
        @RequestBody request: UpdateSessionTitleRequest,
    ): ResponseEntity<ApiResponse<UpdateSessionTitleResponse>> {
        val userId = authentication?.getUserId() ?: 1L // dev 모드: 기본 userId=1
        val updatedSession = aiChatService.updateSessionTitle(sessionId, userId, request.newTitle)
        val response =
            UpdateSessionTitleResponse(
                newTitle = updatedSession.sessionTitle,
            )
        return ResponseEntity.ok(ApiResponse("채팅방 제목이 성공적으로 수정되었습니다.", response))
    }
}
