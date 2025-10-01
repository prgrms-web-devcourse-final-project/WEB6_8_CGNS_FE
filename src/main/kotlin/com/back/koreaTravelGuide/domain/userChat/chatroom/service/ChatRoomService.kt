package com.back.koreaTravelGuide.domain.userChat.chatroom.service

import com.back.koreaTravelGuide.domain.userChat.chatmessage.repository.ChatMessageRepository
import com.back.koreaTravelGuide.domain.userChat.chatroom.dto.ChatRoomListResponse
import com.back.koreaTravelGuide.domain.userChat.chatroom.dto.ChatRoomResponse
import com.back.koreaTravelGuide.domain.userChat.chatroom.entity.ChatRoom
import com.back.koreaTravelGuide.domain.userChat.chatroom.repository.ChatRoomRepository
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.NoSuchElementException

@Service
class ChatRoomService(
    private val roomRepository: ChatRoomRepository,
    private val messageRepository: ChatMessageRepository,
) {
    @Transactional
    fun createOneToOneRoom(
        guideId: Long,
        userId: Long,
        requesterId: Long,
    ): ChatRoom {
        checkParticipant(guideId, userId, requesterId)
        // 1) 기존 방 재사용
        roomRepository.findOneToOneRoom(guideId, userId)?.let { return it }

        // 2) 없으면 생성 (동시요청은 DB 유니크 인덱스로 가드)
        val title = "Guide-$guideId · User-$userId"
        return roomRepository.save(
            ChatRoom(
                title = title,
                guideId = guideId,
                userId = userId,
                updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")),
            ),
        )
    }

    @Transactional(readOnly = true)
    fun get(
        roomId: Long,
        requesterId: Long,
    ): ChatRoom {
        val room =
            roomRepository.findById(roomId)
                .orElseThrow { NoSuchElementException("room not found: $roomId") }
        checkMember(room, requesterId)
        return room
    }

    @Transactional
    fun deleteByOwner(
        roomId: Long,
        requesterId: Long,
    ) {
        val room = get(roomId, requesterId)
        if (room.userId != requesterId) {
            throw AccessDeniedException("채팅방 생성자만 삭제할 수 있습니다.")
        }
        messageRepository.deleteByRoomId(roomId)
        roomRepository.deleteById(roomId)
    }

    @Transactional(readOnly = true)
    fun listRooms(
        requesterId: Long,
        limit: Int,
        cursor: String?,
    ): ChatRoomListResponse {
        val (cursorUpdatedAt, cursorRoomId) = parseCursor(cursor)
        val pageable = PageRequest.of(0, limit)
        val rooms = roomRepository.findPagedByMember(requesterId, cursorUpdatedAt, cursorRoomId, pageable)
        val roomResponses = rooms.map(ChatRoomResponse::from)
        val nextCursor =
            if (roomResponses.size < limit) {
                null
            } else {
                roomResponses.lastOrNull()?.let { last ->
                    last.id?.let { "${last.updatedAt}|$it" }
                }
            }

        return ChatRoomListResponse(
            rooms = roomResponses,
            nextCursor = nextCursor,
        )
    }

    private fun parseCursor(cursor: String?): Pair<ZonedDateTime?, Long?> {
        if (cursor.isNullOrBlank()) {
            return null to null
        }

        val parts = cursor.split("|")
        if (parts.size != 2) {
            return null to null
        }

        val updatedAt = runCatching { ZonedDateTime.parse(parts[0]) }.getOrNull()
        val roomId = runCatching { parts[1].toLong() }.getOrNull()

        return updatedAt to roomId
    }

    private fun checkParticipant(
        guideId: Long,
        userId: Long,
        requesterId: Long,
    ) {
        if (guideId != requesterId && userId != requesterId) {
            throw AccessDeniedException("채팅방은 참여자만 생성할 수 있습니다.")
        }
    }

    private fun checkMember(
        room: ChatRoom,
        requesterId: Long,
    ) {
        if (room.guideId != requesterId && room.userId != requesterId) {
            throw AccessDeniedException("채팅방에 접근할 수 없습니다.")
        }
    }
}
