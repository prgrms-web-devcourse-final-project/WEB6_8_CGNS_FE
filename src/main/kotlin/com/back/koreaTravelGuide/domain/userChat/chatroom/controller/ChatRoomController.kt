package com.back.koreaTravelGuide.domain.userChat.chatroom.controller

import com.back.koreaTravelGuide.domain.userChat.UserChatSseEvents
import com.back.koreaTravelGuide.domain.userChat.chatmessage.service.ChatMessageService
import com.back.koreaTravelGuide.domain.userChat.chatroom.service.ChatRoomService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/userchat/rooms")
class ChatRoomController(
    private val roomSvc: ChatRoomService,
    private val msgSvc: ChatMessageService,
    private val events: UserChatSseEvents,
) {
    // Room API
    @PostMapping
    fun create(
        @RequestBody req: ChatRoomService.CreateRoomReq,
    ) = roomSvc.create(req)

    @GetMapping("/{roomId}")
    fun get(
        @PathVariable roomId: Long,
    ) = roomSvc.get(roomId)

    // Message API (룸 하위 리소스)
    @GetMapping("/{roomId}/messages")
    fun listMessages(
        @PathVariable roomId: Long,
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ) = if (after == null) msgSvc.getlistbefore(roomId, limit) else msgSvc.getlistafter(roomId, after)

    @PostMapping("/{roomId}/messages")
    fun sendMessage(
        @PathVariable roomId: Long,
        @RequestBody req: ChatMessageService.SendMessageReq,
    ) = msgSvc.send(roomId, req).also { saved ->
        events.publishNew(roomId, saved.id!!)
    }

    // SSE 구독도 여기 포함
    @GetMapping("/{roomId}/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(
        @PathVariable roomId: Long,
    ): SseEmitter = events.subscribe(roomId)
}
