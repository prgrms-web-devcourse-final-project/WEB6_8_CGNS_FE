package com.back.koreaTravelGuide.domain.userChat.chatmessage.service

import com.back.koreaTravelGuide.domain.userChat.chatmessage.entity.ChatMessage
import com.back.koreaTravelGuide.domain.userChat.chatmessage.repository.ChatMessageRepository
import com.back.koreaTravelGuide.domain.userChat.chatroom.entity.ChatRoom
import com.back.koreaTravelGuide.domain.userChat.chatroom.repository.ChatRoomRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

@Service
class ChatMessageService(
    private val messageRepository: ChatMessageRepository,
    private val roomRepository: ChatRoomRepository,
) {
    @Transactional(readOnly = true)
    fun getlistbefore(
        roomId: Long,
        limit: Int,
        requesterId: Long,
    ): List<ChatMessage> {
        loadAuthorizedRoom(roomId, requesterId)
        return messageRepository.findTop50ByRoomIdOrderByIdDesc(roomId).asReversed()
    }

    @Transactional(readOnly = true)
    fun getlistafter(
        roomId: Long,
        afterId: Long,
        requesterId: Long,
    ): List<ChatMessage> {
        loadAuthorizedRoom(roomId, requesterId)
        return messageRepository.findByRoomIdAndIdGreaterThanOrderByIdAsc(roomId, afterId)
    }

    @Transactional
    fun send(
        roomId: Long,
        senderId: Long,
        content: String,
    ): ChatMessage {
        val room = loadAuthorizedRoom(roomId, senderId)
        val saved = messageRepository.save(ChatMessage(roomId = roomId, senderId = senderId, content = content))
        roomRepository.save(room.copy(updatedAt = saved.createdAt, lastMessageId = saved.id))
        return saved
    }

    private fun loadAuthorizedRoom(
        roomId: Long,
        memberId: Long,
    ): ChatRoom {
        val room = roomRepository.findById(roomId).orElseThrow { NoSuchElementException("room not found: $roomId") }
        if (room.guideId != memberId && room.userId != memberId) {
            throw AccessDeniedException("user $memberId cannot access room $roomId")
        }
        return room
    }
}
