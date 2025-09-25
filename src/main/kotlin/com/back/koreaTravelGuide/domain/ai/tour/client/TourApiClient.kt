package com.back.koreaTravelGuide.domain.ai.tour.client

import com.back.koreaTravelGuide.domain.ai.tour.dto.InternalData
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

// 09.25 양현준
class TourApiClient(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${tour.api.key}") private val serviceKey: String,
    @Value("\${tour.api.base-url}") private val apiUrl: String,
) {
    // 요청 URL 구성
    private fun buildUrl(params: InternalData): URI =
        UriComponentsBuilder.fromUri(URI.create(apiUrl))
            .path("/areaBasedList2")
            .queryParam("serviceKey", serviceKey)
            .queryParam("MobileOS", "WEB")
            .queryParam("MobileApp", "KoreaTravelGuide")
            .queryParam("_type", "json")
            .queryParam("numOfRows", params.numOfRows)
            .queryParam("pageNo", params.pageNo)
            .queryParam("contentTypeId", params.contentTypeId)
            .queryParam("areaCode", params.areaCode)
            .queryParam("sigunguCode", params.sigunguCode)
            .build()
            .encode()
            .toUri()

    // 지역 기반 관광 정보 조회 (areaBasedList2)
    fun fetchTourInfo(params: InternalData): TourResponse? {
        println("URL 생성")
        val url = buildUrl(params)

        println("관광 정보 조회 API 호출: $url")

        return try {
            val jsonResponse = restTemplate.getForObject(url, String::class.java)
            println("관광 정보 응답 길이: ${jsonResponse?.length ?: 0}")

            if (jsonResponse.isNullOrBlank()) return null // HTTP 호출 결과가 null이거나 공백 문자열일 때

            val root = objectMapper.readTree(jsonResponse) // 문자열을 Jackson 트리 구조(JsonNode)로 변환
            val itemsNode =
                root // path("키") 형태로 노드를 탐색, 응답 Json 형태의 순서에 따라 순차적으로 내려감
                    .path("response")
                    .path("body")
                    .path("items")
                    .path("item")

            if (!itemsNode.isArray || itemsNode.isEmpty) return null // 탐색 결과가 비어 있는 경우

            val firstItem = itemsNode.first()
            TourResponse(
                contentId = firstItem.path("contentid").asText(),
                contentTypeId = firstItem.path("contenttypeid").asText(),
                createdTime = firstItem.path("createdtime").asText(),
                modifiedTime = firstItem.path("modifiedtime").asText(),
                title = firstItem.path("title").asText(),
                addr1 = firstItem.path("addr1").takeIf { it.isTextual }?.asText(),
                areaCode = firstItem.path("areacode").takeIf { it.isTextual }?.asText(),
                firstimage = firstItem.path("firstimage").takeIf { it.isTextual }?.asText(),
                firstimage2 = firstItem.path("firstimage2").takeIf { it.isTextual }?.asText(),
                mapX = firstItem.path("mapx").takeIf { it.isTextual }?.asText(),
                mapY = firstItem.path("mapy").takeIf { it.isTextual }?.asText(),
                mlevel = firstItem.path("mlevel").takeIf { it.isTextual }?.asText(),
                sigunguCode = firstItem.path("sigungucode").takeIf { it.isTextual }?.asText(),
                lDongRegnCd = firstItem.path("lDongRegnCd").takeIf { it.isTextual }?.asText(),
                lDongSignguCd = firstItem.path("lDongSignguCd").takeIf { it.isTextual }?.asText(),
            )
        } catch (e: Exception) {
            println("관광 정보 조회 오류: ${e.message}")
            null
        }
    }
}
