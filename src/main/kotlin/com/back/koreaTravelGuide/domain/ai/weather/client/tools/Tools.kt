package com.back.koreaTravelGuide.domain.ai.weather.client.tools

import org.springframework.stereotype.Component

@Component("clientTools")
class Tools {
    fun getStnIdFromRegionCode(regionCode: String): String {
        return when {
            regionCode.startsWith("11B") -> "109" // 서울,인천,경기도
            regionCode.startsWith("11D1") -> "105" // 강원도영서
            regionCode.startsWith("11D2") -> "105" // 강원도영동
            regionCode.startsWith("11C2") -> "133" // 대전,세종,충청남도
            regionCode.startsWith("11C1") -> "131" // 충청북도
            regionCode.startsWith("11F2") -> "156" // 광주,전라남도
            regionCode.startsWith("11F1") -> "146" // 전북자치도
            regionCode.startsWith("11H1") -> "143" // 대구,경상북도
            regionCode.startsWith("11H2") -> "159" // 부산,울산,경상남도
            regionCode.startsWith("11G") -> "184" // 제주도
            else -> "108" // 전국
        }
    }
}
