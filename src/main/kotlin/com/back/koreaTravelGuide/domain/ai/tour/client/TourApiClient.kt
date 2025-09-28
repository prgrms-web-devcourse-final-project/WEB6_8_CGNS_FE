package com.back.koreaTravelGuide.domain.ai.tour.client

import com.back.koreaTravelGuide.domain.ai.tour.dto.TourItem
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourResponse
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourSearchParams
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

// 09.26 양현준
@Component
class TourApiClient(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${tour.api.key}") private val serviceKey: String,
    @Value("\${tour.api.base-url}") private val apiUrl: String,
) {
    // println 대신 SLF4J 로거 사용
    private val logger = LoggerFactory.getLogger(TourApiClient::class.java)

    // 요청 URL 구성
    private fun buildUrl(params: TourSearchParams): URI =
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
    fun fetchTourInfo(params: TourSearchParams): TourResponse {
        logger.info("지역 기반 관광 정보 조회 시작")

        val url = buildUrl(params)
        logger.info("Tour API URL 생성 : $url")

        /*
         * runCatching: 예외를 Result로 감싸 예외를 던지지 않고 처리하는 유틸리티 함수
         * getOrNull(): 성공 시 응답 문자열을, 실패 시 null 반환
         * takeUnless { it.isNullOrBlank() }: 공백 응답을 걸러냄
         * ?.let { parseItems(it) } ?: emptyList(): 유효한 응답은 파싱, 아니면 빈 리스트 반환
         */
        val response =
            runCatching { restTemplate.getForObject(url, String::class.java) }
                .onFailure { logger.error("관광 정보 조회 실패", it) }
                .getOrNull()
                .takeUnless { it.isNullOrBlank() }
                ?.let { parseItems(it) }

        return response ?: TourResponse(items = emptyList())
    }

    private fun parseItems(json: String): TourResponse {
        val root = objectMapper.readTree(json)

        // header.resultCode 값 추출위한 노스 탐색 과정
        val resultCode =
            root
                .path("response")
                .path("header")
                .path("resultCode")
                .asText()

        // resultCode가 "0000"이 아닌 경우 체크
        if (resultCode != "0000") {
            logger.warn("관광 정보 API resultCode={}", resultCode)
            return TourResponse(items = emptyList())
        }

        // path("키") 형태로 노드를 탐색, 응답 Json 형태의 순서에 따라 순차적으로 내려감
        val itemsNode =
            root
                .path("response")
                .path("body")
                .path("items")
                .path("item")

        // 탐색 결과가 비어 있는 경우
        if (!itemsNode.isArray || itemsNode.isEmpty) return TourResponse(items = emptyList())

        // itemsNode가 배열이므로 map으로 각 노드를 TourItem으로 변환 후 컨테이너로 감싼다.
        val items =
            itemsNode.map { node ->
                TourItem(
                    contentId = node.path("contentid").asText(),
                    contentTypeId = node.path("contenttypeid").asText(),
                    createdTime = node.path("createdtime").asText(),
                    modifiedTime = node.path("modifiedtime").asText(),
                    title = node.path("title").asText(),
                    addr1 = node.path("addr1").takeIf { it.isTextual }?.asText(),
                    areaCode = node.path("areacode").takeIf { it.isTextual }?.asText(),
                    firstimage = node.path("firstimage").takeIf { it.isTextual }?.asText(),
                    firstimage2 = node.path("firstimage2").takeIf { it.isTextual }?.asText(),
                    mapX = node.path("mapx").takeIf { it.isTextual }?.asText(),
                    mapY = node.path("mapy").takeIf { it.isTextual }?.asText(),
                    mlevel = node.path("mlevel").takeIf { it.isTextual }?.asText(),
                    sigunguCode = node.path("sigungucode").takeIf { it.isTextual }?.asText(),
                    lDongRegnCd = node.path("lDongRegnCd").takeIf { it.isTextual }?.asText(),
                    lDongSignguCd = node.path("lDongSignguCd").takeIf { it.isTextual }?.asText(),
                )
            }

        return TourResponse(items = items)
    }
}
