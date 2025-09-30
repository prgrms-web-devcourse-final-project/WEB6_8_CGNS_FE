package com.back.koreaTravelGuide.domain.auth.dto.request

import com.back.koreaTravelGuide.domain.user.enums.UserRole

data class UserRoleUpdateRequest(
    val role: UserRole,
)
