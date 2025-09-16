# 🇰🇷 한국 여행 가이드 챗봇

Spring AI 기반 한국 여행 정보 제공 챗봇 서비스 - OAuth 로그인, 날씨/관광 정보 AI 응답

## 📋 프로젝트 개요

Spring AI 1.0.1과 Groq API를 활용하여 한국 여행에 필요한 정보를 AI가 자동으로 제공하는 챗봇 서비스입니다.
사용자는 OAuth 로그인 후 자연어로 질문하면, AI가 필요에 따라 기상청/관광청 API를 호출하여 답변합니다.

### 🎯 주요 기능

- **🔐 OAuth 로그인**: Google/Kakao/Naver 소셜 로그인
- **💬 채팅 세션 관리**: 대화별 세션 분리 및 기록 저장
- **🤖 AI 자동 도구 호출**: Spring AI `@Tool`로 필요시 자동 API 호출
- **🌤️ 날씨 정보**: 기상청 API 통합 - 중기예보 (4~10일 후)
- **🏛️ 관광 정보**: 관광청 API - 관광지, 축제, 숙박 정보 (예정)
- **📱 실시간 응답**: 한 번에 완성된 응답 제공

## 🏗️ 프로젝트 구조 (DDD)

```
src/main/kotlin/com/back/koreaTravelGuide/
├── application/                          # 애플리케이션 계층
│   └── KoreaTravelGuideApplication.kt    # 메인 앱 + 환경변수 로딩
│
├── domain/                               # 도메인 계층
│   ├── chat/                            # 채팅 도메인
│   │   ├── controller/ChatController.kt  # 채팅 API
│   │   ├── service/ChatService.kt       # 채팅 비즈니스 로직
│   │   ├── dto/                         # 요청/응답 DTO
│   │   ├── entity/                      # 채팅 엔티티
│   │   ├── repository/                  # 채팅 데이터 접근
│   │   └── tool/WeatherTool.kt          # AI 호출 가능 도구들
│   │
│   ├── weather/                         # 날씨 도메인
│   │   ├── service/
│   │   │   ├── WeatherService.kt        # 캐싱 레이어 (12시간)
│   │   │   └── WeatherServiceCore.kt    # 비즈니스 로직
│   │   ├── client/WeatherApiClient.kt   # 기상청 API 클라이언트
│   │   ├── dto/                         # 날씨 응답 구조체
│   │   └── cache/                       # 캐시 설정 (예정)
│   │
│   ├── tour/                            # 관광 도메인 (예정)
│   │   ├── service/
│   │   │   ├── TourService.kt           # 캐싱 레이어
│   │   │   └── TourServiceCore.kt       # 비즈니스 로직
│   │   ├── client/TourApiClient.kt      # 관광청 API 클라이언트
│   │   └── dto/                         # 관광 응답 구조체
│   │
│   └── user/                            # 사용자 도메인 (예정)
│       ├── controller/UserController.kt # 사용자 API
│       ├── service/UserService.kt       # 사용자 관리
│       ├── entity/User.kt               # 사용자 엔티티
│       └── repository/                  # 사용자 데이터 접근
│
├── infrastructure/                      # 인프라 계층
│   └── config/
│       ├── AiConfig.kt                  # Spring AI + Tool 등록
│       ├── RestTemplateConfig.kt        # HTTP 클라이언트
│       └── SecurityConfig.kt            # 보안 설정
│
└── common/                              # 공통 모듈
    ├── ApiResponse.kt                   # 통일된 응답 포맷
    └── exception/
        └── GlobalExceptionHandler.kt    # 전역 예외 처리
```

## 🛠️ 기술 스택

- **Framework**: Spring Boot 3.5.5, Kotlin 1.9.25
- **AI**: Spring AI 1.0.1 + Groq API (openai/gpt-oss-120b)
- **Database**: H2 (개발), JPA + Hibernate
- **Authentication**: Spring Security + OAuth2
- **HTTP Client**: RestTemplate
- **Documentation**: OpenAPI 3.0.3 + Swagger UI
- **Environment**: dotenv-kotlin

## 🚀 빠른 시작

### 1. 환경 변수 설정

`.env` 파일 생성:
```bash
# AI API
GROQ_API_KEY=your_groq_api_key_here

# 기상청 API
WEATHER_API_KEY=your_weather_api_key_here

# OAuth (예정)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

### 2. 애플리케이션 실행

```bash
# 클론 및 의존성 설치
git clone <repository-url>
cd backend

# 실행
./gradlew bootRun
```

### 3. API 확인

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/ai?question=안녕하세요

## 📡 주요 API 엔드포인트

### 🔐 사용자 관리
```http
POST /api/users/oauth/login    # OAuth 로그인
POST /api/users/logout         # 로그아웃
DELETE /api/users/withdrawal   # 회원탈퇴
GET /api/users/profile         # 프로필 조회
```

### 💬 채팅 관리
```http
GET /api/chats/sessions                           # 채팅 세션 목록
POST /api/chats/sessions                          # 새 세션 생성
GET /api/chats/sessions/{sessionId}/messages      # 채팅 기록 조회
POST /api/chats/sessions/{sessionId}/messages     # 메시지 전송 & AI 응답
```

## 🧩 주요 컴포넌트 사용법

### 1. ApiResponse 사용법

모든 API 응답은 `ApiResponse`로 감싸서 반환:

```kotlin
@RestController
class YourController {
    @GetMapping("/test")
    fun test(): ApiResponse<String> {
        return ApiResponse("성공", "데이터")
    }
}

// 응답 형태:
// {
//   "msg": "성공",
//   "data": "데이터"
// }
```

### 2. GlobalExceptionHandler 사용법

예외를 던지기만 하면 자동으로 일관된 에러 응답:

```kotlin
@Service
class YourService {
    fun doSomething() {
        // 400 Bad Request
        throw IllegalArgumentException("잘못된 파라미터입니다")

        // 404 Not Found
        throw NoSuchElementException("데이터를 찾을 수 없습니다")

        // 500 Internal Server Error (모든 예외)
        throw RuntimeException("예상치 못한 오류")
    }
}

// 자동 응답:
// {
//   "msg": "잘못된 파라미터입니다"
// }
```

### 3. AI Tool 추가 방법

새로운 AI 도구 만들기:

```kotlin
// 1. Tool 클래스 생성
@Service
class TourTool(private val tourService: TourService) {

    @Tool(description = "관광지 정보를 조회합니다")
    fun getTourInfo(
        @ToolParam(description = "지역 이름") location: String
    ): TourResponse {
        return tourService.getTourInfo(location)
    }
}

// 2. AiConfig에 등록
@Configuration
class AiConfig {
    @Bean
    fun chatClient(chatModel: ChatModel, weatherTool: WeatherTool, tourTool: TourTool): ChatClient {
        return ChatClient.builder(chatModel)
            .defaultTools(weatherTool, tourTool)  // 여기에 추가
            .build()
    }
}
```

### 4. 새 API 만들기

RestTemplate 사용:

```kotlin
@Component
class YourApiClient(private val restTemplate: RestTemplate) {

    fun callExternalApi(): String? {
        val url = "https://api.example.com/data"
        return restTemplate.getForObject(url, String::class.java)
    }
}
```

### 5. 캐싱 서비스 패턴

Weather/Tour와 동일한 패턴:

```kotlin
// 캐싱 레이어
@Service
class YourService(private val yourServiceCore: YourServiceCore) {
    private var cachedData: YourData? = null
    private var cacheTime: LocalDateTime? = null

    fun getData(): YourData {
        if (shouldRefreshCache()) {
            cachedData = yourServiceCore.fetchData()
            cacheTime = LocalDateTime.now()
        }
        return cachedData!!
    }

    private fun shouldRefreshCache(): Boolean {
        return cacheTime?.isBefore(LocalDateTime.now().minusHours(24)) ?: true
    }
}

// 비즈니스 로직 레이어
@Service
class YourServiceCore(private val yourApiClient: YourApiClient) {
    fun fetchData(): YourData {
        // API 호출 + 데이터 가공
    }
}
```

## 🌤️ 날씨 도구 동작 방식

1. **사용자**: "내일 서울 날씨 어때?"
2. **Spring AI**: 메시지 분석 → WeatherTool 자동 호출
3. **WeatherTool**: `getWeatherForecast("서울")` 실행
4. **WeatherService**: 캐시 확인 → 필요시 WeatherServiceCore 호출
5. **WeatherServiceCore**: 기상청 API 3개 호출 + 데이터 통합
6. **AI**: 날씨 데이터를 포함한 자연스러운 답변 생성

## 📊 데이터베이스 스키마

Mermaid ERD는 `docs/erd-diagram.md` 참조

- **User**: 사용자 정보 (OAuth)
- **ChatSession**: 채팅 세션 (1:N)
- **ChatMessage**: 메시지 내용 (role: user/assistant)

## 🔧 개발 가이드

### 환경별 설정

**개발 환경**: H2 인메모리 DB, 모든 보안 비활성화
**운영 환경**: PostgreSQL, JWT 인증, HTTPS

### 코드 스타일

- **ktlint**: Kotlin 코드 스타일 검사
- **TODO 주석**: 각 파일 상단에 용도 설명
- **DDD 구조**: 도메인별 패키지 분리

### 테스트

```bash
./gradlew test          # 전체 테스트
./gradlew ktlintCheck   # 코드 스타일 검사
```

## 📝 문서

- **API 명세**: `docs/api-specification.yaml`
- **ERD**: `docs/erd-diagram.md`
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

## 🤝 기여 방법

1. 이슈 생성 또는 기존 이슈 확인
2. 브랜치 생성: `feature/기능명` 또는 `fix/버그명`
3. 커밋 메시지: 한글로 명확하게
4. PR 생성: 변경사항과 테스트 결과 포함

## 📞 문의

- **팀**: 한국 여행 가이드 개발팀
- **이슈**: GitHub Issues 활용
- **문서**: `docs/` 폴더 참조

---

**🚀 Spring AI + Kotlin으로 만드는 스마트 여행 가이드!**