package com.back.koreaTravelGuide.domain.user.service

import com.back.koreaTravelGuide.domain.user.dto.request.UserUpdateRequest
import com.back.koreaTravelGuide.domain.user.dto.response.UserResponse
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getUserProfileById(id: Long): UserResponse {
        val user =
            userRepository.findById(id)
                .orElseThrow { NoSuchElementException() }
        return UserResponse.from(user)
    }

    fun updateUserProfile(
        id: Long,
        request: UserUpdateRequest,
    ): UserResponse {
        val user =
            userRepository.findById(id)
                .orElseThrow { NoSuchElementException() }

        user.nickname = request.nickname ?: user.nickname
        user.profileImageUrl = request.profileImageUrl ?: user.profileImageUrl

        return UserResponse.from(userRepository.save(user))
    }

    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw NoSuchElementException()
        }
        userRepository.deleteById(id)
    }
}
