package com.back.koreaTravelGuide.domain.ai.aiChat.controller
import com.back.koreaTravelGuide.config.TestConfig
import com.back.koreaTravelGuide.domain.ai.aiChat.dto.AiChatRequest
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.dotenv
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.transaction.annotation.Transactional

/**
 * AI 채팅 컨트롤러 간단 테스트
 * 응답 구조 확인 및 기본 동작 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@Import(TestConfig::class)
class AiChatControllerTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun loadEnv() {
            val dotenv = dotenv { ignoreIfMissing = true }
            dotenv.entries().forEach { entry ->
                System.setProperty(entry.key, entry.value)
            }
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper = ObjectMapper()
    private val userId = 1L

    @Test
    @WithMockUser
    fun `AI 채팅 기본 동작 테스트`() {
        println("========================================")
        println("🤖 AI 채팅 기본 동작 테스트")
        println("========================================")

        // 1. 세션 생성
        println("1️⃣ 새 채팅방 생성")
        val createResult =
            mockMvc.perform(
                post("/api/aichat/sessions")
                    .param("userId", userId.toString()),
            ).andReturn()

        println("📦 세션 생성 응답 상태: ${createResult.response.status}")
        println("📦 세션 생성 응답 내용: ${createResult.response.contentAsString}")

        if (createResult.response.status != 200) {
            println("❌ 세션 생성 실패 - 테스트 중단")
            return
        }

        // JSON 파싱해서 sessionId 추출
        val createResponse = objectMapper.readTree(createResult.response.contentAsString)
        val sessionId = createResponse.get("data").get("sessionId").asLong()
        println("✅ 세션 생성 완료: sessionId=$sessionId")

        // 2. 간단한 메시지 전송
        println("2️⃣ AI에게 간단한 질문")
        val chatRequest = AiChatRequest("안녕하세요!")

        val messageResult =
            mockMvc.perform(
                post("/api/aichat/sessions/$sessionId/messages")
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(chatRequest)),
            ).andReturn()

        println("📦 메시지 응답 상태: ${messageResult.response.status}")
        println("📦 메시지 응답 내용: ${messageResult.response.contentAsString}")

        if (messageResult.response.status == 200) {
            println("✅ 메시지 전송 성공")
        } else {
            println("❌ 메시지 전송 실패")
        }

        // 3. 세션 목록 조회
        println("3️⃣ 채팅방 목록 조회")
        val sessionsResult =
            mockMvc.perform(
                get("/api/aichat/sessions")
                    .param("userId", userId.toString()),
            ).andReturn()

        println("📦 세션 목록 응답 상태: ${sessionsResult.response.status}")
        if (sessionsResult.response.status == 200) {
            println("✅ 세션 목록 조회 성공")
        } else {
            println("❌ 세션 목록 조회 실패")
        }

        println("🎉 기본 동작 테스트 완료!")
    }

    @Test
    @WithMockUser
    fun `날씨 질문 테스트`() {
        println("========================================")
        println("🌤️ 날씨 질문 테스트")
        println("========================================")

        // 세션 생성
        val createResult =
            mockMvc.perform(
                post("/api/aichat/sessions")
                    .param("userId", userId.toString()),
            ).andReturn()

        if (createResult.response.status != 200) {
            println("❌ 세션 생성 실패")
            return
        }

        val sessionId =
            objectMapper.readTree(createResult.response.contentAsString)
                .get("data").get("sessionId").asLong()

        // 날씨 질문
        val weatherQuestions =
            listOf(
                "서울 날씨 어떤가요?",
                "비 올까요?",
            )

        weatherQuestions.forEachIndexed { index, question ->
            println("💬 질문 ${index + 1}: $question")

            val chatRequest = AiChatRequest(question)
            val result =
                mockMvc.perform(
                    post("/api/aichat/sessions/$sessionId/messages")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)),
                ).andReturn()

            if (result.response.status == 200) {
                println("✅ 응답 성공 (${result.response.contentLength} bytes)")
            } else {
                println("❌ 응답 실패: ${result.response.status}")
            }
        }

        println("🎉 날씨 질문 테스트 완료!")
    }
}
