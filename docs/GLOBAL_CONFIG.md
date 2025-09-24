# 🌐 글로벌 설정 사용법

## 📋 API 응답 & 예외 처리

### ✅ 성공 응답 형식
```kotlin
@RestController
class UserController {
    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: Long): ApiResponse<User> {
        val user = userService.findById(id)
        return ApiResponse("조회 성공", user)
    }
}
```

### ❌ 예외 처리 (자동)
```kotlin
// 예외를 던지면 GlobalExceptionHandler가 자동으로 처리
throw IllegalArgumentException("잘못된 파라미터")  // → 400 Bad Request
throw NoSuchElementException("데이터 없음")      // → 404 Not Found
throw AccessDeniedException("권한 없음")         // → 403 Forbidden
```

### 📱 응답 예시
```json
// 성공
{
  "msg": "조회 성공",
  "data": { "id": 1, "name": "서울" }
}

// 에러
{
  "msg": "데이터를 찾을 수 없습니다",
  "data": null
}
```

## 🛡️ 보안 설정 (SecurityConfig)

### 현재 상태: **개발용 전체 허용**
```kotlin
// 모든 요청 허용 (개발 편의)
.authorizeHttpRequests { auth -> auth.anyRequest().permitAll() }
.csrf { it.disable() }
```

### 운영 시 수정 필요:
- OAuth2 인증 활성화
- CSRF 보호 활성화
- 역할 기반 접근 제어

## 🔧 캐시 설정 (CacheConfig)

### Redis 캐싱 활성화
```kotlin
@Cacheable("weather")    // 30분 캐시
@Cacheable("tour")       // 1시간 캐시
@CacheEvict(value = "weather", allEntries = true)  // 캐시 삭제
```

## 👨‍💻 개발 도구 (DevConfig)

### 시작 시 자동 출력:
- 주요 URL 정보 (Swagger, H2 Console)
- 환경변수 상태 체크
- Redis 서버 설정 가이드
- API 엔드포인트 목록

### 접속 URL:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health

## 🎯 사용법 요약

1. **예외는 그냥 던지세요** - 자동으로 적절한 HTTP 상태 코드로 변환
2. **ApiResponse로 감싸세요** - 일관된 응답 형식 보장
3. **@Cacheable 활용** - API 성능 향상
4. **개발 시작 정보 확인** - 콘솔에서 필요한 모든 정보 제공

**설정 파일 위치**: `src/main/kotlin/com/back/koreaTravelGuide/common/config/`