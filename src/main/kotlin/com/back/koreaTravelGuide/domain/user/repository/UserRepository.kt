package com.back.koreaTravelGuide.domain.user.repository

import com.back.koreaTravelGuide.domain.user.entity.User
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByRole(role: UserRole): List<User>

    fun findByOauthProviderAndOauthId(
        oauthProvider: String,
        oauthId: String,
    ): User?

    fun findByEmail(email: String): User?

    fun findByRoleAndLocationContains(
        role: UserRole,
        location: String,
    ): List<User>
}
