package com.back.koreaTravelGuide.common.exception

import com.back.koreaTravelGuide.common.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * 전역 예외 처리기
 *
 * 모든 컨트롤러에서 발생하는 예외를 통합적으로 처리
 * - 일관된 에러 응답 형식 제공
 * - ApiResponse 래퍼로 감싸서 반환
 * - 개발 시 디버깅 정보 콘솔 출력
 *
 * 각 도메인에서 사용법:
 * ```kotlin
 * @RestController
 * class YourController {
 *     @GetMapping("/test")
 *     fun test(): String {
 *         throw IllegalArgumentException("잘못된 파라미터")  // 자동으로 400 Bad Request 응답
 *         throw NoSuchElementException("데이터 없음")      // 자동으로 404 Not Found 응답
 *     }
 * }
 * ```
 *
 * 커스텀 예외 추가:
 * @ExceptionHandler(YourCustomException::class)로 메서드 추가
 */
@ControllerAdvice
class GlobalExceptionHandler {
    /**
     * @Valid 검증 실패 처리 (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Void>> {
        val message =
            ex.bindingResult
                .allErrors
                .filterIsInstance<FieldError>()
                .joinToString("\n") { error ->
                    "${error.field}: ${error.defaultMessage}"
                }

        return ResponseEntity.badRequest()
            .body(ApiResponse("입력값 검증 실패\n$message"))
    }

    /**
     * JSON 파싱 실패 처리 (400 Bad Request)
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Void>> {
        return ResponseEntity.badRequest()
            .body(ApiResponse("요청 데이터 형식이 올바르지 않습니다"))
    }

    /**
     * 잘못된 파라미터 처리 (400 Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Void>> {
        return ResponseEntity.badRequest()
            .body(ApiResponse(ex.message ?: "잘못된 요청입니다"))
    }

    /**
     * 데이터 없음 처리 (404 Not Found)
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<ApiResponse<Void>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(ex.message ?: "요청한 데이터를 찾을 수 없습니다"))
    }

    /**
     * 리소스 없음 처리 (404 Not Found) - favicon.ico 등
     */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(ex: NoResourceFoundException): ResponseEntity<ApiResponse<Void>> {
        // favicon.ico 요청은 조용히 무시 (로그 안 남김)
        if (ex.message?.contains("favicon.ico") == true) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }

        // 다른 리소스는 로그 남기고 처리
        println("⚠️ 리소스를 찾을 수 없음: ${ex.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse("요청한 리소스를 찾을 수 없습니다"))
    }

    /**
     * 접근 거부 처리 (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiResponse<Void>> {
        println("🚫 접근 거부: ${ex.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse("접근 권한이 없습니다"))
    }

    /**
     * 모든 예외 처리 (500 Internal Server Error)
     * 위에서 처리되지 않은 모든 예외들의 최종 처리
     *
     * 주니어 개발자용 디버깅 정보 추가:
     * - 상세한 에러 스택 트레이스
     * - 요청 정보 로깅
     * - 개발환경에서는 더 자세한 정보 제공
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Map<String, Any?>>> {
        println("❌ 예상치 못한 예외 발생!")
        println("   클래스: ${ex::class.simpleName}")
        println("   메시지: ${ex.message}")
        println("   요청 URL: ${request.method} ${request.requestURL}")
        println("   요청 IP: ${request.remoteAddr}")
        println("   User-Agent: ${request.getHeader("User-Agent")}")

        // 개발환경에서는 스택트레이스도 출력
        ex.printStackTrace()

        // 개발환경에서는 더 자세한 에러 정보 제공 (주니어 개발자 도움용)
        val debugInfo = mutableMapOf<String, Any?>()
        debugInfo["timestamp"] = System.currentTimeMillis()
        debugInfo["path"] = request.requestURI
        debugInfo["method"] = request.method
        debugInfo["error"] = ex::class.simpleName
        debugInfo["message"] = ex.message

        // 스택 트레이스의 첫 3줄만 포함 (너무 길어지지 않도록)
        debugInfo["trace"] = ex.stackTrace.take(3).map { it.toString() }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse("서버 내부 오류가 발생했습니다", debugInfo))
    }
}
