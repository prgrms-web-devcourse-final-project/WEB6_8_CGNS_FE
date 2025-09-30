package com.back.koreaTravelGuide.domain.userChat.chatmessage.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.domain.userChat.chatmessage.dto.ChatMessageResponse
import com.back.koreaTravelGuide.domain.userChat.chatmessage.dto.ChatMessageSendRequest
import com.back.koreaTravelGuide.domain.userChat.chatmessage.service.ChatMessageService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ChatMessageSocketController(
    private val chatMessageService: ChatMessageService,
    private val messagingTemplate: SimpMessagingTemplate,
) {
    @MessageMapping("/userchat/{roomId}/messages")
    fun handleMessage(
        @DestinationVariable roomId: Long,
        principal: Principal,
        @Payload req: ChatMessageSendRequest,
    ) {
        val senderId = principal.name.toLongOrNull() ?: throw AccessDeniedException("인증이 필요합니다.")
        val saved = chatMessageService.send(roomId, senderId, req.content)
        val response = ChatMessageResponse.from(saved)
        messagingTemplate.convertAndSend(
            "/topic/userchat/$roomId",
            ApiResponse(msg = "메시지 전송", data = response),
        )
    }
}
