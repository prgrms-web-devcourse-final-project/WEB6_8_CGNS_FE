package com.back.koreaTravelGuide.common.security

import org.springframework.security.core.Authentication

fun Authentication.getUserId(): Long {
    if (principal is Long) {
        return principal as Long
    }
    throw IllegalStateException("인증된 사용자 ID를 찾을 수 없거나 타입이 올바르지 않습니다.")
}
