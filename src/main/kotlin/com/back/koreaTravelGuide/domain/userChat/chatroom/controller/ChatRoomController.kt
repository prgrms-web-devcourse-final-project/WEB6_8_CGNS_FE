package com.back.koreaTravelGuide.domain.userChat.chatroom.controller

import com.back.koreaTravelGuide.common.ApiResponse
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
    private val roomSvc: ChatRoomService,
) {
    data class StartChatReq(val guideId: Long, val userId: Long)

    data class DeleteChatReq(val userId: Long)

    // 같은 페어는 방 재사용
    @PostMapping("/start")
    fun startChat(
        @RequestBody req: StartChatReq,
    ): ResponseEntity<ApiResponse<Map<String, Long>>> {
        val roomId = roomSvc.exceptOneToOneRoom(req.guideId, req.userId).id!!
        return ResponseEntity.ok(ApiResponse(msg = "채팅방 시작", data = mapOf("roomId" to roomId)))
    }

    @DeleteMapping("/{roomId}")
    fun deleteRoom(
        @PathVariable roomId: Long,
        @RequestBody req: DeleteChatReq,
    ): ResponseEntity<ApiResponse<Unit>> {
        roomSvc.deleteByOwner(roomId, req.userId)
        return ResponseEntity.ok(ApiResponse("채팅방 삭제 완료"))
    }

    @GetMapping("/{roomId}")
    fun get(
        @PathVariable roomId: Long,
    ) = ResponseEntity.ok(ApiResponse(msg = "채팅방 조회", data = roomSvc.get(roomId)))
}
