package com.back.koreaTravelGuide.domain.ai.aiChat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "ai_chat_sessions")
class AiChatSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "session_title", nullable = false, length = 100)
    var sessionTitle: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")),
)
