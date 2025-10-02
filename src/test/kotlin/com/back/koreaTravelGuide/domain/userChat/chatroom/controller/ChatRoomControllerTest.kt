package com.back.koreaTravelGuide.domain.userChat.chatroom.controller

import com.back.koreaTravelGuide.common.security.JwtTokenProvider
import com.back.koreaTravelGuide.domain.user.entity.User
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import com.back.koreaTravelGuide.domain.userChat.chatroom.dto.ChatRoomStartRequest
import com.back.koreaTravelGuide.domain.userChat.chatroom.entity.ChatRoom
import com.back.koreaTravelGuide.domain.userChat.chatroom.repository.ChatRoomRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatRoomControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var chatRoomRepository: ChatRoomRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var guide: User
    private lateinit var guest: User
    private lateinit var guideToken: String
    private lateinit var guestToken: String
    private lateinit var existingRoom: ChatRoom

    @BeforeEach
    fun setUp() {
        chatRoomRepository.deleteAll()
        userRepository.deleteAll()
        guide =
            userRepository.save(
                User(
                    email = "guide@test.com",
                    nickname = "guideUser",
                    role = UserRole.GUIDE,
                    oauthProvider = "test",
                    oauthId = "guide123",
                ),
            )
        guest =
            userRepository.save(
                User(
                    email = "guest@test.com",
                    nickname = "guestUser",
                    role = UserRole.USER,
                    oauthProvider = "test",
                    oauthId = "guest123",
                ),
            )
        guideToken = jwtTokenProvider.createAccessToken(guide.id!!, guide.role)
        guestToken = jwtTokenProvider.createAccessToken(guest.id!!, guest.role)
        existingRoom =
            chatRoomRepository.save(
                ChatRoom(
                    title = "Guide-${guide.id} · User-${guest.id}",
                    guideId = guide.id!!,
                    userId = guest.id!!,
                ),
            )
    }

    @Test
    @DisplayName("listRooms returns paginated rooms with cursor")
    fun listRoomsReturnsPaginatedRooms() {
        val baseTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        val extraRooms =
            (1..6).map { idx ->
                val extraGuide =
                    userRepository.save(
                        User(
                            email = "guide$idx@test.com",
                            nickname = "guide$idx",
                            role = UserRole.GUIDE,
                            oauthProvider = "test",
                            oauthId = "guide$idx",
                        ),
                    )
                chatRoomRepository.save(
                    ChatRoom(
                        title = "Guide-${extraGuide.id} · User-${guest.id}",
                        guideId = extraGuide.id!!,
                        userId = guest.id!!,
                        updatedAt = baseTime.minusMinutes(idx.toLong()),
                    ),
                )
            }

        val firstPage =
            mockMvc.perform(
                get("/api/userchat/rooms")
                    .header("Authorization", "Bearer $guestToken")
                    .param("limit", "3"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.rooms.length()").value(3))
                .andExpect(jsonPath("$.data.rooms[0].id").value(existingRoom.id!!.toInt()))
                .andReturn()

        val firstCursorNode = objectMapper.readTree(firstPage.response.contentAsString)["data"]["nextCursor"]
        assertTrue(firstCursorNode != null && !firstCursorNode.isNull)
        val firstCursor = firstCursorNode.asText()

        mockMvc.perform(
            get("/api/userchat/rooms")
                .header("Authorization", "Bearer $guestToken")
                .param("limit", "3")
                .param("cursor", firstCursor),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.rooms.length()").value(3))
            .andExpect(jsonPath("$.data.rooms[0].id").value(extraRooms[2].id!!.toInt()))
    }

    @Test
    @DisplayName("startChat returns existing room for participant")
    fun startChatReturnsExistingRoom() {
        val body = objectMapper.writeValueAsString(ChatRoomStartRequest(guide.id!!, guest.id!!))
        mockMvc.perform(
            post("/api/userchat/rooms/start")
                .header("Authorization", "Bearer $guestToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(existingRoom.id!!.toInt()))
            .andExpect(jsonPath("$.data.guideId").value(guide.id!!.toInt()))
            .andExpect(jsonPath("$.data.userId").value(guest.id!!.toInt()))
    }

    @Test
    @DisplayName("get returns room for participant")
    fun getReturnsRoomForParticipant() {
        mockMvc.perform(
            get("/api/userchat/rooms/${existingRoom.id}")
                .header("Authorization", "Bearer $guideToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(existingRoom.id!!.toInt()))
            .andExpect(jsonPath("$.data.title").value(existingRoom.title))
    }

    @Test
    @DisplayName("delete removes room when requester is owner")
    fun deleteRemovesRoomWhenOwner() {
        mockMvc.perform(
            delete("/api/userchat/rooms/${existingRoom.id}")
                .header("Authorization", "Bearer $guestToken"),
        )
            .andExpect(status().isOk)
        assertFalse(chatRoomRepository.existsById(existingRoom.id!!))
    }
}
