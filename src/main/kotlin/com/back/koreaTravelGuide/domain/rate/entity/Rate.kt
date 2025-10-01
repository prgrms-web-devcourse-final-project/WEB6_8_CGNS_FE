package com.back.koreaTravelGuide.domain.rate.entity

import com.back.koreaTravelGuide.domain.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "rates")
class Rate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val targetType: RateTargetType,
    @Column(nullable = false)
    val targetId: Long,
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    var rating: Int,
    @Column(columnDefinition = "Text")
    var comment: String?,
    @Column(nullable = false, updatable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")),
    @Column(nullable = false)
    var updatedAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")),
) {
    fun update(
        rating: Int,
        comment: String?,
    ) {
        this.rating = rating
        this.comment = comment
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
    }
}

// /bigint id PK "Auto Increment"
// bigint user_id FK "평가자 ID (GUEST만)"
// enum target_type "AI_CHAT_SESSION, GUIDE"
// bigint target_id "평가 대상 ID (세션 ID 또는 가이드 ID)"
// int rating "평점 1-5"
// text comment "평가 코멘트 (선택사항)"
// datetime created_at "평가 일시"
