package com.back.koreaTravelGuide.domain.userChat.chatroom.service

import com.back.koreaTravelGuide.domain.userChat.chatroom.entity.ChatRoom
import com.back.koreaTravelGuide.domain.userChat.chatroom.repository.ChatRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatRoomService(
    private val roomRepository: ChatRoomRepository,
) {
    data class CreateRoomReq(val title: String, val ownerId: Long)

    @Transactional
    fun create(req: CreateRoomReq): ChatRoom =
        roomRepository.save(ChatRoom(title = req.title, ownerId = req.ownerId, updatedAt = Instant.now()))

    fun get(roomId: Long): ChatRoom = roomRepository.findById(roomId).orElseThrow { NoSuchElementException("room not found: $roomId") }
}
