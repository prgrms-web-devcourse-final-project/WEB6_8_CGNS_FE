package com.back.koreaTravelGuide.common.util

/*
 임시 구현: 테스트 용
 토큰이 "test-token"이면 유효하다고 간주
 완성 후 이 파일은 삭제 또는 대체
 */
object JwtUtil {
    fun validateToken(token: String?): Boolean {
        // 테스트용: "test-token"이면 통과
        return token != null && token == "test-token"
    }

    fun getUserIdFromToken(token: String): String {
        return "user123" // 테스트용 고정 사용자 ID
    }
}
