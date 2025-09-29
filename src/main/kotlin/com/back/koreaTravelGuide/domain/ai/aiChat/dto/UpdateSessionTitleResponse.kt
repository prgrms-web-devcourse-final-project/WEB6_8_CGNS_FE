package com.back.koreaTravelGuide.domain.ai.aiChat.dto

import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatSession

data class UpdateSessionTitleResponse(
    val newTitle: String,
) {
    companion object {
        fun from(session: AiChatSession): UpdateSessionTitleResponse {
            return UpdateSessionTitleResponse(
                newTitle = session.sessionTitle,
            )
        }
    }
}
