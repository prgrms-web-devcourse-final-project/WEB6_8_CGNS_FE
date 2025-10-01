package com.back.koreaTravelGuide.domain.userChat.chatroom.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.domain.userChat.chatroom.dto.ChatRoomListResponse
import com.back.koreaTravelGuide.domain.userChat.chatroom.dto.ChatRoomResponse
import com.back.koreaTravelGuide.domain.userChat.chatroom.dto.ChatRoomStartRequest
import com.back.koreaTravelGuide.domain.userChat.chatroom.service.ChatRoomService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/userchat/rooms")
class ChatRoomController(
    private val roomService: ChatRoomService,
) {
    @GetMapping
    fun listRooms(
        @AuthenticationPrincipal requesterId: Long?,
        @RequestParam(required = false, defaultValue = "20") limit: Int,
        @RequestParam(required = false) cursor: String?,
    ): ResponseEntity<ApiResponse<ChatRoomListResponse>> {
        val authenticatedId = requesterId ?: throw AccessDeniedException("인증이 필요합니다.")
        val safeLimit = limit.coerceIn(1, 100)
        val response = roomService.listRooms(authenticatedId, safeLimit, cursor)
        return ResponseEntity.ok(ApiResponse(msg = "채팅방 목록 조회", data = response))
    }

    // 같은 페어는 방 재사용
    @PostMapping("/start")
    fun startChat(
        @AuthenticationPrincipal requesterId: Long?,
        @RequestBody req: ChatRoomStartRequest,
    ): ResponseEntity<ApiResponse<ChatRoomResponse>> {
        val authenticatedId = requesterId ?: throw AccessDeniedException("인증이 필요합니다.")
        val room = roomService.createOneToOneRoom(req.guideId, req.userId, authenticatedId)
        return ResponseEntity.ok(ApiResponse(msg = "채팅방 시작", data = ChatRoomResponse.from(room)))
    }

    @DeleteMapping("/{roomId}")
    fun deleteRoom(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal requesterId: Long?,
    ): ResponseEntity<ApiResponse<Unit>> {
        val authenticatedId = requesterId ?: throw AccessDeniedException("인증이 필요합니다.")
        roomService.deleteByOwner(roomId, authenticatedId)
        return ResponseEntity.ok(ApiResponse("채팅방 삭제 완료"))
    }

    @GetMapping("/{roomId}")
    fun get(
        @PathVariable roomId: Long,
        @AuthenticationPrincipal requesterId: Long?,
    ): ResponseEntity<ApiResponse<ChatRoomResponse>> {
        val authenticatedId = requesterId ?: throw AccessDeniedException("인증이 필요합니다.")
        val room = roomService.get(roomId, authenticatedId)
        return ResponseEntity.ok(ApiResponse(msg = "채팅방 조회", data = ChatRoomResponse.from(room)))
    }
}
