package com.back.koreaTravelGuide.domain.guide.service

import com.back.koreaTravelGuide.domain.user.dto.request.GuideUpdateRequest
import com.back.koreaTravelGuide.domain.user.dto.response.GuideResponse
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

    @Transactional(readOnly = true)
    fun findGuidesByRegion(region: String): List<GuideResponse> {
        val guides = userRepository.findByRoleAndLocationContains(UserRole.GUIDE, region)
        return guides.map { GuideResponse.from(it) }
    }
}
