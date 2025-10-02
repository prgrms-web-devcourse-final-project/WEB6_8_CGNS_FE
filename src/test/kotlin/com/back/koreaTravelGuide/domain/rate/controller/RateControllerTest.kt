package com.back.koreaTravelGuide.domain.rate.controller

import com.back.koreaTravelGuide.common.security.JwtTokenProvider
import com.back.koreaTravelGuide.domain.ai.aiChat.entity.AiChatSession
import com.back.koreaTravelGuide.domain.ai.aiChat.repository.AiChatSessionRepository
import com.back.koreaTravelGuide.domain.rate.dto.RateRequest
import com.back.koreaTravelGuide.domain.user.entity.User
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RateControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var aiChatSessionRepository: AiChatSessionRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var raterUser: User
    private lateinit var guideUser: User
    private lateinit var adminUser: User
    private lateinit var aiSession: AiChatSession

    private lateinit var raterUserToken: String
    private lateinit var guideUserToken: String
    private lateinit var adminUserToken: String

    @BeforeEach
    fun setUp() {
        raterUser =
            userRepository.save(
                User(email = "rater@test.com", nickname = "rater", role = UserRole.USER, oauthProvider = "test", oauthId = "rater123"),
            )
        guideUser =
            userRepository.save(
                User(email = "guide@test.com", nickname = "guide", role = UserRole.GUIDE, oauthProvider = "test", oauthId = "guide123"),
            )
        adminUser =
            userRepository.save(
                User(email = "admin@test.com", nickname = "admin", role = UserRole.ADMIN, oauthProvider = "test", oauthId = "admin123"),
            )

        aiSession =
            aiChatSessionRepository.save(
                AiChatSession(userId = raterUser.id!!, sessionTitle = "테스트 세션"),
            )

        raterUserToken = jwtTokenProvider.createAccessToken(raterUser.id!!, raterUser.role)
        guideUserToken = jwtTokenProvider.createAccessToken(guideUser.id!!, guideUser.role)
        adminUserToken = jwtTokenProvider.createAccessToken(adminUser.id!!, adminUser.role)
    }

    @Test
    @DisplayName("가이드 평가 생성/수정 성공")
    fun t1() {
        val request = RateRequest(rating = 5, comment = "최고의 가이드")

        mockMvc.perform(
            put("/api/rate/guides/{guideId}", guideUser.id)
                .header("Authorization", "Bearer $raterUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(RateController::class.java))
            .andExpect(handler().methodName("rateGuide"))
            .andExpect(jsonPath("$.data.rating").value(5))
            .andExpect(jsonPath("$.data.comment").value("최고의 가이드"))
    }

    @Test
    @DisplayName("AI 채팅 세션 평가 성공")
    fun t2() {
        val request = RateRequest(rating = 4, comment = "AI가 똑똑하네요.")

        mockMvc.perform(
            put("/api/rate/aichat/sessions/{sessionId}", aiSession.id)
                .header("Authorization", "Bearer $raterUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(RateController::class.java))
            .andExpect(handler().methodName("rateAiSession"))
            .andExpect(jsonPath("$.data.rating").value(4))
    }

    @Test
    @DisplayName("내 가이드 평점 조회 성공")
    fun t3() {
        // given
        val request = RateRequest(rating = 5, comment = "최고의 가이드")
        mockMvc.perform(
            put("/api/rate/guides/{guideId}", guideUser.id)
                .header("Authorization", "Bearer $raterUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )

        // when & then
        mockMvc.perform(
            get("/api/rate/guides/my")
                .header("Authorization", "Bearer $guideUserToken"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(RateController::class.java))
            .andExpect(handler().methodName("getMyGuideRatings"))
            .andExpect(jsonPath("$.data.totalRatings").value(1))
            .andExpect(jsonPath("$.data.averageRating").value(5.0))
    }

    @Test
    @DisplayName("관리자의 AI 채팅 평가 목록 조회 성공")
    fun t4() {
        // given
        val request = RateRequest(rating = 4, comment = "AI가 똑똑하네요.")
        mockMvc.perform(
            put("/api/rate/aichat/sessions/{sessionId}", aiSession.id)
                .header("Authorization", "Bearer $raterUserToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )

        // when & then
        mockMvc.perform(
            get("/api/rate/admin/aichat/sessions?page=0&size=10")
                .header("Authorization", "Bearer $adminUserToken"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(handler().handlerType(RateController::class.java))
            .andExpect(handler().methodName("getAllAiSessionRatings"))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.totalElements").value(1))
    }
}
