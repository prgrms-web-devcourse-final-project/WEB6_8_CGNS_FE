package com.back.koreaTravelGuide.domain.userChat.chatmessage.dto

import com.back.koreaTravelGuide.domain.userChat.chatmessage.entity.ChatMessage
import java.time.ZoneId
import java.time.ZonedDateTime

data class ChatMessageResponse(
    val id: Long?,
    val roomId: Long,
    val senderId: Long,
    val content: String,
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")),
) {
    companion object {
        fun from(message: ChatMessage): ChatMessageResponse {
            return ChatMessageResponse(
                id = message.id,
                roomId = message.roomId,
                senderId = message.senderId,
                content = message.content,
                createdAt = message.createdAt,
            )
        }
    }
}
