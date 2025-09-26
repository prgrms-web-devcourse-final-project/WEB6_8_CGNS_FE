package com.back.koreaTravelGuide.domain.ai.aiChat.dto

import com.back.koreaTravelGuide.domain.ai.aiChat.entity.SenderType

data class SessionMessagesResponse(
    val content: String,
    val senderType: SenderType,
)
