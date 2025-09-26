package com.back.koreaTravelGuide.domain.userChat.chatroom.repository

import com.back.koreaTravelGuide.domain.userChat.chatroom.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomRepository : JpaRepository<ChatRoom, Long>
