package com.back.koreaTravelGuide.domain.ai.weather.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "weather.region")
data class RegionCodeProperties(
    var codes: Map<String, String> = emptyMap(),
) {
    fun getCodeByLocation(location: String): String {
        return codes[location] ?: "11B10101"
    }
}
