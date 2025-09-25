package com.back.koreaTravelGuide.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/chat")
            .addInterceptors(JwtHandsshakeInterceptor) // JWT 체크
            .setAllowedOrigins("*")
            .withSockJS() // 개발 중 호환성
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/sub") // 구독 경로
        registry.setApplicationDestinationPrefixes("/app") // 클라이언트에서 보낼 때
    }
}
