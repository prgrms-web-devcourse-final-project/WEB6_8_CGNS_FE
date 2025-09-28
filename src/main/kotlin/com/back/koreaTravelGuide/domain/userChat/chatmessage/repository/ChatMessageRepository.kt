package com.back.koreaTravelGuide.domain.userChat.chatmessage.repository

import com.back.koreaTravelGuide.domain.userChat.chatmessage.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findTop50ByRoomIdOrderByIdDesc(roomId: Long): List<ChatMessage>

    fun findByRoomIdAndIdGreaterThanOrderByIdAsc(
        roomId: Long,
        afterId: Long,
    ): List<ChatMessage>

    fun deleteByRoomId(roomId: Long)
}
