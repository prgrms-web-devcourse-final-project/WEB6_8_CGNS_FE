package com.back.koreaTravelGuide.domain.userChat

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

// Websocket,Stomp 사용 전 임시로 만들었음
// 테스트 후 제거 예정

@Component
class UserChatSseEvents {
    private val emitters = ConcurrentHashMap<Long, MutableList<SseEmitter>>()

    fun subscribe(roomId: Long): SseEmitter {
        val emitter = SseEmitter(0L)
        emitters.computeIfAbsent(roomId) { mutableListOf() }.add(emitter)
        emitter.onCompletion { emitters[roomId]?.remove(emitter) }
        emitter.onTimeout { emitter.complete() }
        return emitter
    }

    fun publishNew(
        roomId: Long,
        lastMessageId: Long,
    ) {
        emitters[roomId]?.toList()?.forEach {
            try {
                it.send(SseEmitter.event().name("NEW").data(lastMessageId))
            } catch (_: Exception) {
                it.complete()
            }
        }
    }
}
