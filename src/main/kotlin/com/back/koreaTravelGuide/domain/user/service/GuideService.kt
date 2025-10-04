package com.back.koreaTravelGuide.domain.guide.service

import com.back.koreaTravelGuide.domain.user.dto.request.GuideUpdateRequest
import com.back.koreaTravelGuide.domain.user.dto.response.GuideResponse
import com.back.koreaTravelGuide.domain.user.enums.Region
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GuideService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getAllGuides(): List<GuideResponse> {
        return userRepository.findByRole(UserRole.GUIDE)
            .map { GuideResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getGuideById(guideId: Long): GuideResponse {
        val user =
            userRepository.findById(guideId)
                .orElseThrow { NoSuchElementException() }

        if (user.role != UserRole.GUIDE) {
            throw IllegalArgumentException()
        }
        return GuideResponse.from(user)
    }

    fun updateGuideProfile(
        guideId: Long,
        request: GuideUpdateRequest,
    ): GuideResponse {
        val user =
            userRepository.findById(guideId)
                .orElseThrow { NoSuchElementException() }

        if (user.role != UserRole.GUIDE) {
            throw IllegalAccessException()
        }

        user.nickname = request.nickname ?: user.nickname
        user.profileImageUrl = request.profileImageUrl ?: user.profileImageUrl
        user.location = request.location ?: user.location
        user.description = request.description ?: user.description

        return GuideResponse.from(userRepository.save(user))
    }

    // 김지원: 10월 4일 임시 수정 - upstream merge 후 Region enum 타입 불일치 해결
    // String -> Region enum 변환 추가
    @Transactional(readOnly = true)
    fun findGuidesByRegion(region: String): List<GuideResponse> {
        val regionEnum = Region.valueOf(region.uppercase())
        val guides = userRepository.findByRoleAndLocation(UserRole.GUIDE, regionEnum)
        return guides.map { GuideResponse.from(it) }
    }
}
