package com.back.koreaTravelGuide.domain.user.dto.response

import com.back.koreaTravelGuide.domain.user.entity.User
import com.back.koreaTravelGuide.domain.user.enums.Region
import com.back.koreaTravelGuide.domain.user.enums.UserRole

data class GuideResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val profileImageUrl: String?,
    val role: UserRole,
    val location: Region?,
    val description: String?,
) {
    companion object {
        fun from(user: User): GuideResponse {
            return GuideResponse(
                id = user.id!!,
                email = user.email,
                nickname = user.nickname,
                profileImageUrl = user.profileImageUrl,
                role = user.role,
                location = user.location,
                description = user.description,
            )
        }
    }
}
