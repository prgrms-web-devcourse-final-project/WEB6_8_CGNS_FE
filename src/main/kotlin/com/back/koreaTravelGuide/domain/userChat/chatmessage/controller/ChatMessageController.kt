package com.back.koreaTravelGuide.domain.userChat.chatmessage.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.domain.userChat.chatmessage.service.ChatMessageService
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
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
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): ResponseEntity<ApiResponse<Any>> {
        val messages =
            if (after == null) {
                messageService.getlistbefore(roomId, limit)
            } else {
                messageService.getlistafter(roomId, after)
            }
        return ResponseEntity.ok(ApiResponse(msg = "메시지 조회", data = messages))
    }

    @PostMapping("/{roomId}/messages")
    fun sendMessage(
        @PathVariable roomId: Long,
        @RequestBody req: ChatMessageService.SendMessageReq,
    ): ResponseEntity<ApiResponse<Any>> {
        val saved = messageService.send(roomId, req)
        messagingTemplate.convertAndSend(
            "/topic/userchat/$roomId",
            ApiResponse(msg = "메시지 전송", data = saved),
        )
        return ResponseEntity.status(201).body(ApiResponse(msg = "메시지 전송", data = saved))
    }
}
