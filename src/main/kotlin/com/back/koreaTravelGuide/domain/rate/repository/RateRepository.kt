package com.back.koreaTravelGuide.domain.rate.repository

import com.back.koreaTravelGuide.domain.rate.entity.Rate
import com.back.koreaTravelGuide.domain.rate.entity.RateTargetType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RateRepository : JpaRepository<Rate, Long> {
    fun findByTargetTypeAndTargetIdAndUserId(
        targetType: RateTargetType,
        targetId: Long,
        userId: Long,
    ): Rate?

    fun findByTargetTypeAndTargetId(
        targetType: RateTargetType,
        targetId: Long,
    ): List<Rate>

    fun findByTargetType(
        targetType: RateTargetType,
        pageable: Pageable,
    ): Page<Rate>
}
