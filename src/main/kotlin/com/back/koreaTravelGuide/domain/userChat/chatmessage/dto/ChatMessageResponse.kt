package com.back.koreaTravelGuide.domain.userChat.chatmessage.dto

import com.back.koreaTravelGuide.domain.userChat.chatmessage.entity.ChatMessage
import java.time.Instant

data class ChatMessageResponse(
    val id: Long?,
    val roomId: Long,
    val senderId: Long,
    val content: String,
    val createdAt: Instant,
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
