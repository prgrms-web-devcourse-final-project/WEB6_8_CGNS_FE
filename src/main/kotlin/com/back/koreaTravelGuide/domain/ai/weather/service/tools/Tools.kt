package com.back.koreaTravelGuide.domain.weather.service.tools

import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Component
open class Tools {
    fun getRegionCodeFromLocation(location: String): String {
        return REGION_MAP[location] ?: "11B10101"
    }

    fun validBaseTime(baseTime: String?): String {
        val actualBaseTime =
            if (baseTime != null && (baseTime.endsWith("0600") || baseTime.endsWith("1800"))) {
                println("📌 제공된 발표시각 사용: $baseTime")
                baseTime
            } else {
                if (baseTime != null) {
                    println("⚠️ 잘못된 발표시각 무시: $baseTime (06시 또는 18시만 유효)")
                }
                getCurrentBaseTime()
            }
        return actualBaseTime
    }

    private fun getCurrentBaseTime(): String {
        // 한국시간(KST) 기준으로 현재 시간 계산
        val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val hour = now.hour

        println("🕐 현재 KST 시간: ${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")

        return if (hour < 6) {
            // 06시 이전이면 전날 18시 발표
            val baseTime = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "1800"
            println("📅 사용할 발표시각: $baseTime (전날 18시)")
            baseTime
        } else if (hour < 18) {
            // 06시~18시 사이면 당일 06시 발표
            val baseTime = now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "0600"
            println("📅 사용할 발표시각: $baseTime (당일 06시)")
            baseTime
        } else {
            // 18시 이후면 당일 18시 발표
            val baseTime = now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "1800"
            println("📅 사용할 발표시각: $baseTime (당일 18시)")
            baseTime
        }
    }

    companion object {
        private val REGION_MAP =
            mapOf(
                "서울" to "11B10101", "인천" to "11B20201", "수원" to "11B20601", "파주" to "11B20305",
                "이천" to "11B20612", "평택" to "11B20606", "춘천" to "11D10301", "원주" to "11D10401",
                "강릉" to "11D20501", "속초" to "11D20601", "대전" to "11C20401", "세종" to "11C20404",
                "청주" to "11C10301", "충주" to "11C10101", "전주" to "11F10201", "군산" to "11F10501",
                "광주" to "11F20501", "목포" to "11F20401", "여수" to "11F20801", "대구" to "11H10701",
                "안동" to "11H10501", "포항" to "11H10201", "부산" to "11H20201", "울산" to "11H20101",
                "창원" to "11H20301", "통영" to "11H20401", "제주" to "11G00201", "서귀포" to "11G00401",
            )
    }
}
