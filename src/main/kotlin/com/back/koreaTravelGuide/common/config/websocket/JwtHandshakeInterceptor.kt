package com.back.koreaTravelGuide.common.config.websocket

import com.back.koreaTravelGuide.common.util.JwtUtil
import org.springframework.http.HttpMethod
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder
import java.lang.Exception
import java.net.URI

@Component
class JwtHandshakeInterceptor : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>,
    ): Boolean {
        val uri: URI = request.uri
        val method = request.method

        // ✅ SockJS 호환을 위한 임시 우회 (브라우저 테스트용)
        // - SockJS는 /info 요청을 토큰 없이 보냄
        // - 추후 순수 WebSocket 전환 시 제거 예정
        if (HttpMethod.OPTIONS == method || uri.path.endsWith("/info")) {
            return true
        }

        val token =
            UriComponentsBuilder.fromUri(request.uri)
                .build()
                .queryParams
                .getFirst("token")
                ?: return false // 토큰 없으면 연결 거부

        return if (JwtUtil.validateToken(token)) {
            val userId = JwtUtil.getUserIdFromToken(token)
            attributes["userId"] = userId
            true
        } else {
            false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?,
    ) {
    }
}
