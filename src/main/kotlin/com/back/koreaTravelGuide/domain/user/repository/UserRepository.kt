package com.back.koreaTravelGuide.domain.user.repository

import com.back.koreaTravelGuide.domain.user.entity.User
import com.back.koreaTravelGuide.domain.user.enums.Region
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

    // 김지원: 10월 4일 임시 수정 - upstream merge 후 Region enum 타입 불일치 해결
    // findByRoleAndLocationContains -> findByRoleAndLocation으로 변경
    // location 파라미터: String -> Region enum
    fun findByRoleAndLocation(
        role: UserRole,
        location: Region,
    ): List<User>
}
