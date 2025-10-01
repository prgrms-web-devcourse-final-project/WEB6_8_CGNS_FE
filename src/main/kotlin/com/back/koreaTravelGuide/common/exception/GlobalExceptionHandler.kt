package com.back.koreaTravelGuide.common.exception

import com.back.koreaTravelGuide.common.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * @Valid 검증 실패 → 400
 * throw IllegalArgumentException("메시지") → 400
 * throw NoSuchElementException("메시지") → 404
 * throw IllegalStateException("메시지") → 409
 * 기타 모든 예외 → 500
 */
@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Void>> {
        val message =
            ex.bindingResult.allErrors.filterIsInstance<FieldError>()
                .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        logger.warn("입력값 검증 실패: {}", message)
        return ResponseEntity.badRequest().body(ApiResponse("입력값 검증 실패: $message"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Void>> {
        logger.warn("잘못된 파라미터: {}", ex.message)
        return ResponseEntity.badRequest().body(ApiResponse(ex.message ?: "잘못된 요청입니다"))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<ApiResponse<Void>> {
        logger.warn("데이터 없음: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse(ex.message ?: "데이터를 찾을 수 없습니다"))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ApiResponse<Void>> {
        logger.warn("부적절한 상태: {}", ex.message)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse(ex.message ?: "요청을 처리할 수 없는 상태입니다"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse<Void>> {
        // Static resource 예외는 무시 (favicon.ico, CSS, JS 등)
        if (ex is org.springframework.web.servlet.resource.NoResourceFoundException) {
            logger.debug("Static resource not found: {} at {}", ex.message, request.requestURI)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse("리소스를 찾을 수 없습니다"))
        }

        logger.error("서버 오류 - {}: {} at {}", ex::class.simpleName, ex.message, request.requestURI, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse("서버 내부 오류가 발생했습니다"))
    }
}
