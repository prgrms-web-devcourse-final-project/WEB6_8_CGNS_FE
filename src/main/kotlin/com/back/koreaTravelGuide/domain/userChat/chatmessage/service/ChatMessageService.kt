package com.back.koreaTravelGuide.domain.userChat.chatmessage.service

import com.back.koreaTravelGuide.domain.userChat.chatmessage.entity.ChatMessage
import com.back.koreaTravelGuide.domain.userChat.chatmessage.repository.ChatMessageRepository
import com.back.koreaTravelGuide.domain.userChat.chatroom.repository.ChatRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val messageRepository: ChatMessageRepository,
    private val roomRepository: ChatRoomRepository,
) {
    @Transactional(readOnly = true)
    fun getlistbefore(
        roomId: Long,
        limit: Int,
    ): List<ChatMessage> = messageRepository.findTop50ByRoomIdOrderByIdDesc(roomId).asReversed()

    @Transactional(readOnly = true)
    fun getlistafter(
        roomId: Long,
        afterId: Long,
    ): List<ChatMessage> = messageRepository.findByRoomIdAndIdGreaterThanOrderByIdAsc(roomId, afterId)

    @Transactional
    fun send(
        roomId: Long,
        senderId: Long,
        content: String,
    ): ChatMessage {
        val saved = messageRepository.save(ChatMessage(roomId = roomId, senderId = senderId, content = content))
        roomRepository.findById(roomId).ifPresent {
            roomRepository.save(it.copy(updatedAt = saved.createdAt, lastMessageId = saved.id))
        }
        return saved
    }
}
