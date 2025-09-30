package com.back.koreaTravelGuide.domain.ai.weather.client.builder

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class UrlBuilder(
    @Value("\${weather.api.key}") private val serviceKey: String,
    @Value("\${weather.api.base-url}") private val apiUrl: String,
) {
    fun buildMidFcstUrl(
        stnId: String,
        tmFc: String,
    ): String =
        UriComponentsBuilder.fromUriString("$apiUrl/getMidFcst")
            .queryParam("pageNo", 1)
            .queryParam("numOfRows", 10)
            .queryParam("dataType", "JSON")
            .queryParam("stnId", stnId)
            .queryParam("tmFc", tmFc)
            .queryParam("authKey", serviceKey)
            .toUriString()

    fun buildMidTaUrl(
        regId: String,
        tmFc: String,
    ): String =
        UriComponentsBuilder.fromUriString("$apiUrl/getMidTa")
            .queryParam("pageNo", 1)
            .queryParam("numOfRows", 10)
            .queryParam("dataType", "JSON")
            .queryParam("regId", regId)
            .queryParam("tmFc", tmFc)
            .queryParam("authKey", serviceKey)
            .toUriString()

    fun buildMidLandFcstUrl(
        regId: String,
        tmFc: String,
    ): String =
        UriComponentsBuilder.fromUriString("$apiUrl/getMidLandFcst")
            .queryParam("pageNo", 1)
            .queryParam("numOfRows", 10)
            .queryParam("dataType", "JSON")
            .queryParam("regId", regId)
            .queryParam("tmFc", tmFc)
            .queryParam("authKey", serviceKey)
            .toUriString()
}
