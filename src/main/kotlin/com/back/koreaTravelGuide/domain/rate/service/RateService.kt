package com.back.koreaTravelGuide.domain.rate.service

import com.back.koreaTravelGuide.domain.ai.aiChat.repository.AiChatSessionRepository
import com.back.koreaTravelGuide.domain.rate.dto.GuideRatingSummaryResponse
import com.back.koreaTravelGuide.domain.rate.dto.RateResponse
import com.back.koreaTravelGuide.domain.rate.entity.Rate
import com.back.koreaTravelGuide.domain.rate.entity.RateTargetType
import com.back.koreaTravelGuide.domain.rate.repository.RateRepository
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RateService(
    private val rateRepository: RateRepository,
    private val userRepository: UserRepository,
    private val aiChatSessionRepository: AiChatSessionRepository,
) {
    // 가이드 평가
    fun rateGuide(
        raterUserId: Long,
        guideId: Long,
        rating: Int,
        comment: String?,
    ): Rate {
        val guide =
            userRepository.findByIdOrNull(guideId)
                ?: throw NoSuchElementException("해당 가이드를 찾을 수 없습니다.")
        if (guide.role != UserRole.GUIDE) {
            throw IllegalArgumentException("평가 대상은 가이드이어야 합니다.")
        }

        val rater =
            userRepository.findByIdOrNull(raterUserId)
                ?: throw NoSuchElementException("평가자를 찾을 수 없습니다.")
        if (rater.role != UserRole.USER) {
            throw IllegalArgumentException("유저만 가이드를 평가할 수 있습니다.")
        }

        val existingRate =
            rateRepository.findByTargetTypeAndTargetIdAndUserId(
                targetType = RateTargetType.GUIDE,
                targetId = guideId,
                userId = raterUserId,
            )

        return if (existingRate != null) {
            existingRate.update(rating, comment)
            rateRepository.save(existingRate)
        } else {
            val newRate =
                Rate(
                    user = rater,
                    targetType = RateTargetType.GUIDE,
                    targetId = guideId,
                    rating = rating,
                    comment = comment,
                )
            rateRepository.save(newRate)
        }
    }

    // ai 평가
    fun rateAiSession(
        raterUserId: Long,
        sessionId: Long,
        rating: Int,
        comment: String?,
    ): Rate {
        val session =
            aiChatSessionRepository.findByIdOrNull(sessionId)
                ?: throw NoSuchElementException("해당 AI 채팅 세션을 찾을 수 없습니다.")

        if (session.userId != raterUserId) {
            throw IllegalArgumentException("세션 소유자만 평가할 수 있습니다.")
        }

        val rater =
            userRepository.findByIdOrNull(raterUserId)
                ?: throw NoSuchElementException("평가자를 찾을 수 없습니다.")

        val existingRate =
            rateRepository.findByTargetTypeAndTargetIdAndUserId(
                targetType = RateTargetType.AI_SESSION,
                targetId = sessionId,
                userId = raterUserId,
            )

        return if (existingRate != null) {
            existingRate.update(rating, comment)
            rateRepository.save(existingRate)
        } else {
            val newRate =
                Rate(
                    user = rater,
                    targetType = RateTargetType.AI_SESSION,
                    targetId = sessionId,
                    rating = rating,
                    comment = comment,
                )
            rateRepository.save(newRate)
        }
    }

    // 가이드 평점 매기기
    @Transactional(readOnly = true)
    fun getGuideRatingSummary(guideId: Long): GuideRatingSummaryResponse {
        val ratings = rateRepository.findByTargetTypeAndTargetId(RateTargetType.GUIDE, guideId)

        if (ratings.isEmpty()) {
            return GuideRatingSummaryResponse(0.0, 0, emptyList())
        }

        val totalRatings = ratings.size
        val averageRating = ratings.sumOf { it.rating } / totalRatings.toDouble()

        // 소수점 2자리에서 반올림
        val roundedAverage = String.format("%.1f", averageRating).toDouble()

        val rateResponses = ratings.map { RateResponse.from(it) }

        return GuideRatingSummaryResponse(
            averageRating = roundedAverage,
            totalRatings = totalRatings,
            ratings = rateResponses,
        )
    }

    // 가이드 평점 조회
    @Transactional(readOnly = true)
    fun getMyGuideRatingSummary(guideId: Long): GuideRatingSummaryResponse {
        val guide =
            userRepository.findByIdOrNull(guideId)
                ?: throw NoSuchElementException("사용자를 찾을 수 없습니다.")
        if (guide.role != UserRole.GUIDE) {
            throw IllegalArgumentException("가이드만 자신의 평점을 조회할 수 있습니다.")
        }
        return getGuideRatingSummary(guideId)
    }

    // ai 평점 조회
    @Transactional(readOnly = true)
    fun getAllAiSessionRatingsForAdmin(pageable: Pageable): Page<RateResponse> {
        val ratingsPage = rateRepository.findByTargetType(RateTargetType.AI_SESSION, pageable)
        return ratingsPage.map { RateResponse.from(it) }
    }
}
