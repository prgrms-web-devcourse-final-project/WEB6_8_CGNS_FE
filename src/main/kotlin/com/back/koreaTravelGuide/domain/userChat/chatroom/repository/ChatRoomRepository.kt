package com.back.koreaTravelGuide.domain.userChat.chatroom.repository

import com.back.koreaTravelGuide.domain.userChat.chatroom.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomRepository : JpaRepository<ChatRoom, Long>
