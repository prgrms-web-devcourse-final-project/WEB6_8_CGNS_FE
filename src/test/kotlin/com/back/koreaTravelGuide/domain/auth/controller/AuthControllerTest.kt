package com.back.koreaTravelGuide.domain.auth.controller
import com.back.koreaTravelGuide.common.security.JwtTokenProvider
import com.back.koreaTravelGuide.config.TestConfig
import com.back.koreaTravelGuide.domain.user.entity.User
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestConfig::class)
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var pendingUser: User
    private lateinit var generalUser: User

    @BeforeEach
    fun setUp() {
        pendingUser =
            userRepository.save(
                User(
                    email = "pending@test.com",
                    nickname = "pendingUser",
                    role = UserRole.PENDING,
                    oauthProvider = "test",
                    oauthId = "test1234",
                ),
            )

        generalUser =
            userRepository.save(
                User(
                    email = "user@test.com",
                    nickname = "generalUser",
                    role = UserRole.USER,
                    oauthProvider = "test",
                    oauthId = "test5678",
                ),
            )
    }

    @Test
    @DisplayName("신규 사용자 역할 선택 성공")
    fun t1() {
        // given
        val registerToken = jwtTokenProvider.createRegisterToken(pendingUser.id!!)
        val requestBody = mapOf("role" to UserRole.USER)

        // when & then
        mockMvc.perform(
            post("/api/auth/role")
                .header("Authorization", "Bearer $registerToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("역할이 선택되었으며 로그인에 성공했습니다."))
            .andExpect(jsonPath("$.data.accessToken").exists())
    }

    @Test
    @DisplayName("로그아웃 성공")
    fun t2() {
        // given
        val accessToken = jwtTokenProvider.createAccessToken(generalUser.id!!, generalUser.role)

        // when & then
        mockMvc.perform(
            post("/api/auth/logout")
                .header("Authorization", "Bearer $accessToken"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."))

        // verify
        val isBlacklisted = redisTemplate.opsForValue().get(accessToken) != null
        assertTrue(isBlacklisted)
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    fun t3() {
        // given
        val refreshToken = jwtTokenProvider.createRefreshToken(generalUser.id!!)
        val redisKey = "refreshToken:${generalUser.id}"
        redisTemplate.opsForValue().set(redisKey, refreshToken, 7, TimeUnit.DAYS)

        // when & then
        mockMvc.perform(
            post("/api/auth/refresh")
                .cookie(Cookie("refreshToken", refreshToken)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("Access Token이 성공적으로 재발급되었습니다."))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(cookie().exists("refreshToken"))
    }
}
