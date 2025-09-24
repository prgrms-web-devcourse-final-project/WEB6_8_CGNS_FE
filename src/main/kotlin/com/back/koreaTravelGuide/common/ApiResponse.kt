package com.back.koreaTravelGuide.common

// TODO: API 응답 래퍼 - 모든 API 응답을 일관된 형태로 감싸는 공통 구조체
data class ApiResponse<T>(
    val msg: String,
    val data: T? = null,
) {
    constructor(msg: String) : this(msg, null)
}
