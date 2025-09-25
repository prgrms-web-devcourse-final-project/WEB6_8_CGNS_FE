package com.back.koreaTravelGuide.domain.user.service

import com.back.koreaTravelGuide.domain.user.dto.request.UserUpdateRequest
import com.back.koreaTravelGuide.domain.user.dto.response.UserResponse
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
) {
    // 회원 이메일 단건 조회
    @Transactional(readOnly = true)
    fun getUserProfileByEmail(email: String): UserResponse {
        val user =
            userRepository.findByEmail(email)
                ?: throw EntityNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다")
        return UserResponse.from(user)
    }

    // 회원 업데이트
    fun updateProfile(
        email: String,
        request: UserUpdateRequest,
    ): UserResponse {
        val user =
            userRepository.findByEmail(email)
                ?: throw EntityNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다")

        user.nickname = request.nickname ?: user.nickname
        user.profileImageUrl = request.profileImageUrl ?: user.profileImageUrl
        user.location = request.location ?: user.location
        user.description = request.description ?: user.description

        val updatedUser = userRepository.save(user)
        return UserResponse.from(updatedUser)
    }

    // 회원 삭제
    fun deleteUser(email: String) {
        val user =
            userRepository.findByEmail(email)
                ?: throw EntityNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다")
        userRepository.delete(user)
    }

    // 전체 가이드 목록 조회
    @Transactional(readOnly = true)
    fun getAllGuides(): List<UserResponse> {
        return userRepository.findByRole(UserRole.GUIDE)
            .map { UserResponse.from(it) }
    }

    // 가이드 정보 단건 조회
    @Transactional(readOnly = true)
    fun getGuideById(guideId: Long): UserResponse {
        val user =
            userRepository.findById(guideId)
                .orElseThrow { EntityNotFoundException("해당 ID의 사용자를 찾을 수 없습니다") }
        return UserResponse.from(user)
    }
}
