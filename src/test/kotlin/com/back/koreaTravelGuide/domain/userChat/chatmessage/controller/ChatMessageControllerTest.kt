package com.back.koreaTravelGuide.domain.userChat.chatmessage.controller
import com.back.koreaTravelGuide.common.security.JwtTokenProvider
import com.back.koreaTravelGuide.config.TestConfig
import com.back.koreaTravelGuide.domain.user.entity.User
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import com.back.koreaTravelGuide.domain.userChat.chatmessage.dto.ChatMessageSendRequest
import com.back.koreaTravelGuide.domain.userChat.chatmessage.entity.ChatMessage
import com.back.koreaTravelGuide.domain.userChat.chatmessage.repository.ChatMessageRepository
import com.back.koreaTravelGuide.domain.userChat.chatroom.entity.ChatRoom
import com.back.koreaTravelGuide.domain.userChat.chatroom.repository.ChatRoomRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestConfig::class)
class ChatMessageControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var chatMessageRepository: ChatMessageRepository

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
    private lateinit var room: ChatRoom
    private lateinit var firstMessage: ChatMessage

    @BeforeEach
    fun setUp() {
        chatMessageRepository.deleteAll()
        chatRoomRepository.deleteAll()
        userRepository.deleteAll()
        guide =
            userRepository.save(
                User(
                    email = "guide2@test.com",
                    nickname = "guideTwo",
                    role = UserRole.GUIDE,
                    oauthProvider = "test",
                    oauthId = "guide456",
                ),
            )
        guest =
            userRepository.save(
                User(
                    email = "guest2@test.com",
                    nickname = "guestTwo",
                    role = UserRole.USER,
                    oauthProvider = "test",
                    oauthId = "guest456",
                ),
            )
        guideToken = jwtTokenProvider.createAccessToken(guide.id!!, guide.role)
        guestToken = jwtTokenProvider.createAccessToken(guest.id!!, guest.role)
        room =
            chatRoomRepository.save(
                ChatRoom(
                    title = "Guide-${guide.id} · User-${guest.id}",
                    guideId = guide.id!!,
                    userId = guest.id!!,
                ),
            )
        firstMessage =
            chatMessageRepository.save(
                ChatMessage(
                    roomId = room.id!!,
                    senderId = guide.id!!,
                    content = "첫 메세지",
                ),
            )
    }

    @Test
    @DisplayName("listMessages returns latest batch when after is missing")
    fun listMessagesReturnsLatest() {
        mockMvc.perform(
            get("/api/userchat/rooms/${room.id}/messages")
                .header("Authorization", "Bearer $guestToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].id").value(firstMessage.id!!.toInt()))
            .andExpect(jsonPath("$.data[0].content").value(firstMessage.content))
    }

    @Test
    @DisplayName("listMessages filters newer messages when after provided")
    fun listMessagesFiltersNewer() {
        val newer =
            chatMessageRepository.save(
                ChatMessage(
                    roomId = room.id!!,
                    senderId = guest.id!!,
                    content = "두 번째",
                ),
            )

        mockMvc.perform(
            get("/api/userchat/rooms/${room.id}/messages")
                .param("after", firstMessage.id!!.toString())
                .header("Authorization", "Bearer $guestToken"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].id").value(newer.id!!.toInt()))
            .andExpect(jsonPath("$.data[0].content").value(newer.content))
    }

    @Test
    @DisplayName("sendMessage stores message and updates room summary")
    fun sendMessageStoresMessage() {
        val before = chatMessageRepository.count()
        val body = objectMapper.writeValueAsString(ChatMessageSendRequest("새 메세지"))

        mockMvc.perform(
            post("/api/userchat/rooms/${room.id}/messages")
                .header("Authorization", "Bearer $guestToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.content").value("새 메세지"))

        val messages = chatMessageRepository.findAll()
        assertEquals(before + 1, messages.size.toLong())
        val latest = messages.maxByOrNull { it.id ?: 0L }!!
        assertEquals(latest.id, chatRoomRepository.findById(room.id!!).get().lastMessageId)
    }
}
