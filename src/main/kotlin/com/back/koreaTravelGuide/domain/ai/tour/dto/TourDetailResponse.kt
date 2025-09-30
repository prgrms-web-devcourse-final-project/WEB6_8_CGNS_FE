package com.back.koreaTravelGuide.domain.ai.tour.dto

data class TourDetailResponse(
    val items: List<TourDetailItem>,
)

data class TourDetailItem(
    val contentId: String,
    val title: String,
    val overview: String?,
    val addr1: String?,
    val mapX: String?,
    val mapY: String?,
    val firstImage: String?,
    val tel: String?,
    val homepage: String?,
)
