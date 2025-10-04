package com.back.koreaTravelGuide.domain.user.dto.request

import com.back.koreaTravelGuide.domain.user.enums.Region

data class GuideUpdateRequest(
    val nickname: String?,
    val profileImageUrl: String?,
    val location: Region?,
    val description: String?,
)
