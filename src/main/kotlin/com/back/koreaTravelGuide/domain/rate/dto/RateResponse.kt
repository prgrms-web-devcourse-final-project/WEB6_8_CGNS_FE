package com.back.koreaTravelGuide.domain.rate.dto

import com.back.koreaTravelGuide.domain.rate.entity.Rate
import java.time.ZonedDateTime

data class RateResponse(
    val id: Long,
    val raterNickname: String,
    val rating: Int,
    val comment: String?,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(rate: Rate): RateResponse {
            return RateResponse(
                id = rate.id!!,
                raterNickname = rate.user.nickname,
                rating = rate.rating,
                comment = rate.comment,
                createdAt = rate.createdAt,
            )
        }
    }
}
