package com.back.koreaTravelGuide.domain.ai.weather.service.tools

import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Component("serviceTools")
class Tools() {
    fun getCurrentBaseTime(): String {
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val baseHour =
            when {
                now.hour < 6 -> "1800" // 06시 이전 → 전날 18시
                now.hour < 18 -> "0600" // 06시~18시 → 당일 06시
                else -> "1800" // 18시 이후 → 당일 18시
            }

        val baseDate = if (now.hour < 6) now.minusDays(1) else now
        return baseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + baseHour
    }
}
