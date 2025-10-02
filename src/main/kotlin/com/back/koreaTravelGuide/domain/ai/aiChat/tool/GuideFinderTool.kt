package com.back.koreaTravelGuide.domain.ai.aiChat.tool

import com.back.koreaTravelGuide.common.logging.log
import com.back.koreaTravelGuide.domain.guide.service.GuideService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

@Component
class GuideFinderTool(
    private val guideService: GuideService,
    private val objectMapper: ObjectMapper,
) {
    @Tool(description = "특정 지역(region)에서 활동하는 여행 가이드 목록을 검색합니다.")
    fun findGuidesByRegion(
        @ToolParam(description = "검색할 지역 이름. 예: '서울', '부산', '강남구'", required = true)
        region: String,
    ): String {
        log.info("🔧 [TOOL CALLED] findGuidesByRegion - region: $region")

        val guides = guideService.findGuidesByRegion(region)

        return try {
            if (guides.isEmpty()) {
                log.info("✅ [TOOL RESULT] findGuidesByRegion - 결과 없음")
                return "해당 지역에서 활동하는 가이드를 찾을 수 없습니다."
            }
            val result = objectMapper.writeValueAsString(guides)
            log.info("✅ [TOOL RESULT] findGuidesByRegion - 결과: ${result.take(200)}...")
            result
        } catch (e: Exception) {
            log.error("❌ [TOOL ERROR] findGuidesByRegion - 예외 발생: ${e.javaClass.name}", e)
            "가이드 정보를 JSON으로 변환하는 중 오류가 발생했습니다."
        }
    }
}
