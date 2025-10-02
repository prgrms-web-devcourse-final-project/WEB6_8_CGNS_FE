package com.back.koreaTravelGuide.domain.userChat.chatroom.dto

data class ChatRoomListResponse(
    val rooms: List<ChatRoomResponse>,
    val nextCursor: String?,
)
