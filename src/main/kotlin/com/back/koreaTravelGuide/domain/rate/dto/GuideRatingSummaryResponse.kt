package com.back.koreaTravelGuide.domain.rate.dto

data class GuideRatingSummaryResponse(
    val averageRating: Double,
    val totalRatings: Int,
    val ratings: List<RateResponse>,
)
