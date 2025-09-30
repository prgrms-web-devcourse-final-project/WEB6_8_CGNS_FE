package com.back.koreaTravelGuide.domain.userChat.config

import com.back.koreaTravelGuide.common.security.JwtTokenProvider
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Component

@Component
class UserChatStompAuthChannelInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
) : ChannelInterceptor {
    override fun preSend(
        message: Message<*>,
        channel: MessageChannel,
    ): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java) ?: return message

        if (accessor.command == StompCommand.CONNECT) {
            val rawHeader =
                accessor.getFirstNativeHeader("Authorization")
                    ?: throw AuthenticationCredentialsNotFoundException("Authorization header is missing")
            val token = rawHeader.removePrefix("Bearer ").trim()
            if (!jwtTokenProvider.validateToken(token)) {
                throw AuthenticationCredentialsNotFoundException("Invalid JWT token")
            }
            accessor.user = jwtTokenProvider.getAuthentication(token)
        } else if (accessor.user == null) {
            throw AuthenticationCredentialsNotFoundException("Unauthenticated STOMP request")
        }

        return message
    }
}
