package com.back.koreaTravelGuide.domain.userChat.chatmessage.service

import com.back.koreaTravelGuide.domain.userChat.chatmessage.entity.ChatMessage
import com.back.koreaTravelGuide.domain.userChat.chatmessage.repository.ChatMessageRepository
import com.back.koreaTravelGuide.domain.userChat.chatroom.repository.ChatRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val msgRepository: ChatMessageRepository,
    private val roomRepository: ChatRoomRepository,
) {
    data class SendMessageReq(val senderId: Long, val content: String)

    fun getlistbefore(
        roomId: Long,
        limit: Int,
    ): List<ChatMessage> = msgRepository.findTop50ByRoomIdOrderByIdDesc(roomId).asReversed()

    fun getlistafter(
        roomId: Long,
        afterId: Long,
    ): List<ChatMessage> = msgRepository.findByRoomIdAndIdGreaterThanOrderByIdAsc(roomId, afterId)

    @Transactional
    fun deleteByRoom(roomId: Long) {
        msgRepository.deleteByRoomId(roomId)
    }

    @Transactional
    fun send(
        roomId: Long,
        req: SendMessageReq,
    ): ChatMessage {
        val saved = msgRepository.save(ChatMessage(roomId = roomId, senderId = req.senderId, content = req.content))
        roomRepository.findById(roomId).ifPresent {
            roomRepository.save(it.copy(updatedAt = saved.createdAt, lastMessageId = saved.id))
        }
        return saved
    }
}
