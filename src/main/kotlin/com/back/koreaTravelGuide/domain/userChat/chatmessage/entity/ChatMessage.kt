package com.back.koreaTravelGuide.domain.userChat.chatmessage.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "userchat_message",
    indexes = [Index(name = "ix_msg_room_id_id", columnList = "room_id,id")],
)
data class ChatMessage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "room_id", nullable = false)
    val roomId: Long,
    @Column(name = "sender_id", nullable = false)
    val senderId: Long,
    @Column(columnDefinition = "text", nullable = false)
    val content: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
