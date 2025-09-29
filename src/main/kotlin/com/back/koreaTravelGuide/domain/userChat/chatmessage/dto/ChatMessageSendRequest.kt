package com.back.koreaTravelGuide.domain.userChat.chatmessage.dto

data class ChatMessageSendRequest(
    val senderId: Long,
    val content: String,
)
