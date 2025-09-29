package com.back.koreaTravelGuide.domain.userChat.chatroom.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.domain.userChat.chatroom.dto.ChatRoomDeleteRequest
import com.back.koreaTravelGuide.domain.userChat.chatroom.dto.ChatRoomResponse
import com.back.koreaTravelGuide.domain.userChat.chatroom.dto.ChatRoomStartRequest
import com.back.koreaTravelGuide.domain.userChat.chatroom.service.ChatRoomService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/userchat/rooms")
class ChatRoomController(
    private val roomService: ChatRoomService,
) {
    // 같은 페어는 방 재사용
    @PostMapping("/start")
    fun startChat(
        @RequestBody req: ChatRoomStartRequest,
    ): ResponseEntity<ApiResponse<ChatRoomResponse>> {
        val room = roomService.exceptOneToOneRoom(req.guideId, req.userId)
        return ResponseEntity.ok(ApiResponse(msg = "채팅방 시작", data = ChatRoomResponse.from(room)))
    }

    @DeleteMapping("/{roomId}")
    fun deleteRoom(
        @PathVariable roomId: Long,
        @RequestBody req: ChatRoomDeleteRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        roomService.deleteByOwner(roomId, req.userId)
        return ResponseEntity.ok(ApiResponse("채팅방 삭제 완료"))
    }

    @GetMapping("/{roomId}")
    fun get(
        @PathVariable roomId: Long,
    ): ResponseEntity<ApiResponse<ChatRoomResponse>> {
        val room = roomService.get(roomId)
        return ResponseEntity.ok(ApiResponse(msg = "채팅방 조회", data = ChatRoomResponse.from(room)))
    }
}
