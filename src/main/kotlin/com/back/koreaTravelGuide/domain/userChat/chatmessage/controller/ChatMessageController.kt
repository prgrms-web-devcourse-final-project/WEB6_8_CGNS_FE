package com.back.koreaTravelGuide.domain.userChat.chatmessage.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.domain.userChat.chatmessage.dto.ChatMessageResponse
import com.back.koreaTravelGuide.domain.userChat.chatmessage.dto.ChatMessageSendRequest
import com.back.koreaTravelGuide.domain.userChat.chatmessage.service.ChatMessageService
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/userchat/rooms")
class ChatMessageController(
    private val messageService: ChatMessageService,
    private val messagingTemplate: SimpMessagingTemplate,
) {
    @GetMapping("/{roomId}/messages")
    fun listMessages(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal requesterId: Long?,
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): ResponseEntity<ApiResponse<List<ChatMessageResponse>>> {
        val memberId = requesterId ?: throw AccessDeniedException("인증이 필요합니다.")
        val messages =
            if (after == null) {
                messageService.getlistbefore(roomId, limit, memberId)
            } else {
                messageService.getlistafter(roomId, after, memberId)
            }
        val responseMessages = messages.map(ChatMessageResponse::from)
        return ResponseEntity.ok(ApiResponse(msg = "메시지 조회", data = responseMessages))
    }

    @PostMapping("/{roomId}/messages")
    fun sendMessage(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal senderId: Long?,
        @RequestBody req: ChatMessageSendRequest,
    ): ResponseEntity<ApiResponse<ChatMessageResponse>> {
        val memberId = senderId ?: throw AccessDeniedException("인증이 필요합니다.")
        val saved = messageService.send(roomId, memberId, req.content)
        val response = ChatMessageResponse.from(saved)
        messagingTemplate.convertAndSend(
            "/topic/userchat/$roomId",
            ApiResponse(msg = "메시지 전송", data = response),
        )
        return ResponseEntity.status(201).body(ApiResponse(msg = "메시지 전송", data = response))
    }
}
