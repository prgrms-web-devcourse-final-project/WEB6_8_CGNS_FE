package com.back.koreaTravelGuide.domain.user.dto.request

data class GuideUpdateRequest(
    val nickname: String?,
    val profileImageUrl: String?,
    val location: String?,
    val description: String?,
)
