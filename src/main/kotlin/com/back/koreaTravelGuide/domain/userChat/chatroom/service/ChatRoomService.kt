package com.back.koreaTravelGuide.domain.userChat.chatroom.service

import com.back.koreaTravelGuide.domain.userChat.chatmessage.repository.ChatMessageRepository
import com.back.koreaTravelGuide.domain.userChat.chatroom.entity.ChatRoom
import com.back.koreaTravelGuide.domain.userChat.chatroom.repository.ChatRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatRoomService(
    private val roomRepository: ChatRoomRepository,
    private val messageRepository: ChatMessageRepository,
) {
    data class CreateRoomRequest(val title: String, val guideId: Long, val userId: Long)

    @Transactional
    fun exceptOneToOneRoom(
        guideId: Long,
        userId: Long,
    ): ChatRoom {
        // 1) 기존 방 재사용
        roomRepository.findOneToOneRoom(guideId, userId)?.let { return it }

        // 2) 없으면 생성 (동시요청은 DB 유니크 인덱스로 가드)
        val title = "Guide-$guideId · User-$userId"
        return roomRepository.save(
            ChatRoom(title = title, guideId = guideId, userId = userId, updatedAt = Instant.now()),
        )
    }

    fun get(roomId: Long): ChatRoom =
        roomRepository.findById(roomId)
            .orElseThrow { NoSuchElementException("room not found: $roomId") }

    @Transactional
    fun deleteByOwner(
        roomId: Long,
        requesterId: Long,
    ) {
        val room = get(roomId)
        if (room.userId != requesterId) {
            // 예외처리 임시
            throw IllegalArgumentException("채팅방 생성자만 삭제할 수 있습니다.")
        }
        messageRepository.deleteByRoomId(roomId)
        roomRepository.deleteById(roomId)
    }
}
