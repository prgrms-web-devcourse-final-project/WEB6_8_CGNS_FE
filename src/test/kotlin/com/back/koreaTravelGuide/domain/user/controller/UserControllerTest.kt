package com.back.koreaTravelGuide.domain.user.controller
import com.back.koreaTravelGuide.common.security.JwtTokenProvider
import com.back.koreaTravelGuide.config.TestConfig
import com.back.koreaTravelGuide.domain.user.entity.User
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestConfig::class)
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var testUser: User
    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        testUser =
            userRepository.save(
                User(
                    email = "testuser@test.com",
                    nickname = "testUser",
                    role = UserRole.USER,
                    oauthProvider = "test",
                    oauthId = "test12345",
                ),
            )
        accessToken = jwtTokenProvider.createAccessToken(testUser.id!!, testUser.role)
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    fun t1() {
        // when & then
        mockMvc.perform(
            get("/api/users/me")
                .header("Authorization", "Bearer $accessToken"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.email").value(testUser.email))
            .andExpect(jsonPath("$.data.nickname").value(testUser.nickname))
    }

    @Test
    @DisplayName("내 프로필 수정 성공")
    fun t2() {
        // given
        val updatedNickname = "updatedUser"
        val requestBody = mapOf("nickname" to updatedNickname)

        // when & then
        mockMvc.perform(
            patch("/api/users/me")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.nickname").value(updatedNickname))
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    fun t3() {
        // when & then
        mockMvc.perform(
            delete("/api/users/me")
                .header("Authorization", "Bearer $accessToken"),
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.msg").value("회원 탈퇴가 완료되었습니다."))

        // verify
        val userExists = userRepository.existsById(testUser.id!!)
        assertFalse(userExists)
    }
}
