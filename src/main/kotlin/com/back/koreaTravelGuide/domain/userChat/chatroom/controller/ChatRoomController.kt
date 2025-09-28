package com.back.koreaTravelGuide.domain.userChat.chatroom.controller

import com.back.koreaTravelGuide.common.ApiResponse
import com.back.koreaTravelGuide.domain.userChat.UserChatSseEvents
import com.back.koreaTravelGuide.domain.userChat.chatmessage.service.ChatMessageService
import com.back.koreaTravelGuide.domain.userChat.chatroom.service.ChatRoomService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

// 컨트롤러는 임시로 강사님 스타일 따라서 통합해놓았음. 추후 리팩토링 예정
@RestController
@RequestMapping("/api/userchat/rooms")
class ChatRoomController(
    private val roomSvc: ChatRoomService,
    private val msgSvc: ChatMessageService,
    private val events: UserChatSseEvents,
) {
    data class StartChatReq(val guideId: Long, val userId: Long)

    data class DeleteChatReq(val userId: Long)

    // MVP: 같은 페어는 방 재사용
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

    @GetMapping("/{roomId}/messages")
    fun listMessages(
        @PathVariable roomId: Long,
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): ResponseEntity<ApiResponse<Any>> {
        val messages =
            if (after == null) {
                msgSvc.getlistbefore(roomId, limit)
            } else {
                msgSvc.getlistafter(roomId, after)
            }
        return ResponseEntity.ok(ApiResponse(msg = "메시지 조회", data = messages))
    }

    @PostMapping("/{roomId}/messages")
    fun sendMessage(
        @PathVariable roomId: Long,
        @RequestBody req: ChatMessageService.SendMessageReq,
    ): ResponseEntity<ApiResponse<Any>> {
        val saved = msgSvc.send(roomId, req)
        events.publishNew(roomId, saved.id!!)
        return ResponseEntity.status(201).body(ApiResponse(msg = "메시지 전송", data = saved))
    }

    // SSE는 스트림이여서 ApiResponse로 감싸지 않았음
    // WebSocket,Stomp 적용되면 바로 삭제 예정
    @GetMapping("/{roomId}/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(
        @PathVariable roomId: Long,
    ): SseEmitter = events.subscribe(roomId)
}
