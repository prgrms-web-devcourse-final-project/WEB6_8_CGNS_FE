package com.back.koreaTravelGuide.domain.ai.aiChat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "ai_chat_messages")
class AiChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    val aiChatSession: AiChatSession,
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    val content: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    val senderType: SenderType,
    @Column(name = "created_at", nullable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")),
)
