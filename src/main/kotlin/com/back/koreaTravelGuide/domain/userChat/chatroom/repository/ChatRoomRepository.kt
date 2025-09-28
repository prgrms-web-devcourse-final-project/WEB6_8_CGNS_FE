package com.back.koreaTravelGuide.domain.userChat.chatroom.repository

import com.back.koreaTravelGuide.domain.userChat.chatroom.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomRepository : JpaRepository<ChatRoom, Long> {
    // 가이드,유저 방 생성시 중복 생성 방지
    @Query(
        """
        select r from ChatRoom r
        where (r.guideId = :guideId and r.userId = :userId)
           or (r.guideId = :userId and r.userId = :guideId)
        """,
    )
    fun findOneToOneRoom(
        guideId: Long,
        userId: Long,
    ): ChatRoom?
}
