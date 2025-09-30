package com.back.koreaTravelGuide.domain.userChat.chatroom.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(
    name = "userchat_room",
    indexes = [Index(name = "ix_room_updated_at", columnList = "updated_at")],
)
data class ChatRoom(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val title: String,
    @Column(name = "guide_id", nullable = false)
    val guideId: Long,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")),
    @Column(name = "last_message_id")
    val lastMessageId: Long? = null,
)
