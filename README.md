# 🇰🇷 한국 여행 가이드 챗봇 - 팀 개발 가이드

Spring AI 기반 한국 여행 정보 제공 챗봇 서비스 개발 가이드 (내부용)

## 📋 프로젝트 개요

Spring AI 1.0.1 + Groq API를 활용한 스마트 여행 챗봇
- OAuth 로그인 → 채팅 세션 → AI가 자동으로 날씨/관광 정보 제공
- DDD 아키텍처로 도메인별 개발 담당 분리

## 🏗️ 프로젝트 구조

```
src/main/kotlin/com/back/koreaTravelGuide/
├── application/
│   └── KoreaTravelGuideApplication.kt    # 메인 앱 (환경변수 로딩)
│
├── domain/                               # 🎯 각 도메인별 담당 분리
│   ├── chat/                            # 💬 채팅 도메인 담당
│   │   ├── controller/ChatController.kt  # 채팅 API
│   │   ├── service/ChatService.kt       # 채팅 비즈니스 로직
│   │   ├── dto/                         # 채팅 요청/응답 DTO
│   │   ├── entity/ChatHistory.kt        # 채팅 엔티티
│   │   ├── repository/ChatRepository.kt # 채팅 데이터 접근
│   │   └── tool/WeatherTool.kt          # 🔧 AI Tool 함수들 (Spring AI @Tool)
│   │
│   ├── weather/                         # 🌤️ 날씨 도메인 (구현 완료)
│   │   ├── service/WeatherService.kt    # @Cacheable 기반 캐싱 + 비즈니스 로직
│   │   ├── client/WeatherApiClient.kt   # 기상청 API 클라이언트
│   │   └── dto/                         # 날씨 응답 구조체
│   │
│   ├── tour/                            # 🏛️ 관광 도메인 담당
│   │   ├── service/
│   │   │   ├── TourService.kt           # 캐싱 레이어 (구현 필요)
│   │   │   └── TourServiceCore.kt       # API 호출 + 데이터 처리
│   │   ├── client/TourApiClient.kt      # 관광청 API 클라이언트
│   │   ├── dto/                         # 관광 응답 구조체
│   │   └── cache/                       # 캐시 설정
│   │
│   └── user/                            # 👤 사용자 도메인 담당
│       ├── controller/UserController.kt # OAuth 로그인 API
│       ├── service/UserService.kt       # 사용자 관리
│       ├── entity/User.kt               # 사용자 엔티티
│       ├── dto/                         # 사용자 요청/응답 DTO
│       └── repository/UserRepository.kt # 사용자 데이터 접근
│
├── infrastructure/                      # 인프라 설정 (공통)
│   └── config/
│       ├── AiConfig.kt                  # Spring AI + Tool 등록
│       ├── CacheConfig.kt               # @Cacheable 캐시 설정
│       ├── RestTemplateConfig.kt        # HTTP 클라이언트 설정
│       └── SecurityConfig.kt            # 보안 설정
│
└── common/                              # 공통 유틸리티
    ├── ApiResponse.kt                   # 통일 응답 포맷
    └── exception/GlobalExceptionHandler.kt # 전역 예외 처리
```

## 👥 도메인별 담당자 가이드

### 🔧 Tool 도메인 담당자

**현재 상태**: WeatherTool 구현 완료
**담당 파일**: `domain/chat/tool/WeatherTool.kt`

#### 🚀 WeatherTool 리팩토링 계획

**현재 구조**: 1개 Tool로 3개 API 통합
```kotlin
@Tool(description = "중기 날씨 예보를 조회합니다")
fun getWeatherForecast(location: String) {
    // 3개 API 동시 호출 → 데이터 통합
}
```

**리팩토링 목표**: 3개 Tool로 분리하여 스마트 호출
```kotlin
// Tool 1: 전국 중기 전망 (108 stnId)
@Tool(description = "전국 중기 날씨 전망을 조회합니다")
fun getNationalWeatherForecast() {
    // getMidFcst API 호출 (stnId=108)
    // AI가 날씨 좋은 지역 판단
}

// Tool 2: 특정 지역 강수 정보
@Tool(description = "특정 지역의 강수 확률을 조회합니다")
fun getRegionalRainInfo(regionCode: String) {
    // getMidLandFcst API 호출
}

// Tool 3: 특정 지역 기온 정보
@Tool(description = "특정 지역의 기온 정보를 조회합니다")
fun getRegionalTemperature(regionCode: String) {
    // getMidTa API 호출
}
```

**동작 방식**:
1. 사용자: "여행하기 좋은 날씨 지역 추천해줘"
2. AI: Tool1 호출 → 전국 날씨 분석 → 좋은 지역 선별
3. AI: Tool2, Tool3 호출 → 선별된 지역의 상세 정보 수집
4. AI: 통합 분석하여 추천 답변 생성

### 💬 채팅 도메인 담당자

**담당 폴더**: `domain/chat/`
**구현해야 할 것들**:

#### 1. ChatController 완성
```kotlin
@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService,
    private val chatClient: ChatClient
) {
    @GetMapping("/sessions")
    fun getSessions(): ApiResponse<List<ChatSessionResponse>> {
        // 사용자별 채팅 세션 목록 조회
    }

    @PostMapping("/sessions")
    fun createSession(@RequestBody request: CreateSessionRequest): ApiResponse<ChatSessionResponse> {
        // 새 채팅 세션 생성
    }

    @GetMapping("/sessions/{sessionId}/messages")
    fun getMessages(@PathVariable sessionId: Long): ApiResponse<ChatHistoryResponse> {
        // 채팅 기록 조회
    }

    @PostMapping("/sessions/{sessionId}/messages")
    fun sendMessage(
        @PathVariable sessionId: Long,
        @RequestBody request: SendMessageRequest
    ): ApiResponse<ChatMessageResponse> {
        // 메시지 전송 + AI 응답 생성
        // chatClient.prompt().system(systemPrompt).user(message).call()
    }
}
```

#### 2. Entity 설계 (ERD 참고)
```kotlin
@Entity
@Table(name = "chat_sessions")
class ChatSession(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id")
    val userId: Long,

    val title: String,

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "chat_messages")
class ChatMessage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "session_id")
    val sessionId: Long,

    @Column(columnDefinition = "TEXT")
    val content: String,

    @Enumerated(EnumType.STRING)
    val role: MessageRole, // USER, ASSISTANT

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class MessageRole { USER, ASSISTANT }
```

#### 3. Service 레이어
```kotlin
@Service
class ChatService(
    private val chatSessionRepository: ChatSessionRepository,
    private val chatMessageRepository: ChatMessageRepository
) {
    fun createSession(userId: Long, title: String?): ChatSession {
        // 새 세션 생성
    }

    fun saveMessage(sessionId: Long, content: String, role: MessageRole): ChatMessage {
        // 메시지 저장
    }

    fun getSessionMessages(sessionId: Long): List<ChatMessage> {
        // 세션의 모든 메시지 조회
    }
}
```

### 🏛️ 투어 도메인 담당자

**담당 폴더**: `domain/tour/`
**참고**: Weather 도메인과 동일한 구조로 구현

#### 1. 관광청 API 연동
```kotlin
@Component
class TourApiClient(
    private val restTemplate: RestTemplate,
    @Value("\${tour.api.key}") private val serviceKey: String // .env의 WEATHER_API_KEY와 동일
) {
    fun fetchTourInfo(areaCode: String, contentTypeId: String): String? {
        val url = "https://apis.data.go.kr/B551011/KorService1/areaBasedList1" +
                "?serviceKey=$serviceKey&areaCode=$areaCode&contentTypeId=$contentTypeId"
        return restTemplate.getForObject(url, String::class.java)
    }
}
```

#### 2. @Cacheable 캐싱 구조 (Weather 패턴 따라하기)
```kotlin
// TourService.kt - @Cacheable 적용
@Service
class TourService(private val tourApiClient: TourApiClient) {

    @Cacheable("tour", key = "#areaCode + '_' + #contentType")
    fun getTourInfo(areaCode: String, contentType: String): TourResponse {
        // 관광청 API 호출 + 데이터 가공 로직
        return tourApiClient.fetchTourInfo(areaCode, contentType)
    }

    @CacheEvict("tour", allEntries = true)
    @Scheduled(fixedRate = 86400000) // 24시간마다 캐시 삭제
    fun clearTourCache() {
        println("🗑️ 관광 캐시 자동 삭제 완료")
    }
}
```

### 👤 사용자 도메인 담당자

**담당 폴더**: `domain/user/`
**구현해야 할 것들**:

#### 1. OAuth 로그인 구현
```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping("/oauth/login")
    fun oauthLogin(@RequestBody request: OAuthLoginRequest): ApiResponse<LoginResponse> {
        // OAuth 로그인 처리
    }

    @PostMapping("/logout")
    fun logout(): ApiResponse<Void> {
        // 로그아웃 처리
    }

    @DeleteMapping("/withdrawal")
    fun withdrawal(): ApiResponse<Void> {
        // 회원탈퇴 처리
    }

    @GetMapping("/profile")
    fun getProfile(): ApiResponse<UserResponse> {
        // 프로필 조회
    }
}
```

#### 2. User Entity (ERD 참고)
```kotlin
@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    val username: String,

    @Column(unique = true)
    val email: String,

    val nickname: String,

    @Column(name = "profile_image")
    val profileImage: String?,

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

## 🛠️ 공통 개발 가이드

### 캐시 시스템 (현재 적용됨)

**✅ Spring @Cacheable 어노테이션 방식 적용**

#### 현재 구현된 캐시 시스템

**1. CacheConfig.kt 설정**
```kotlin
@Configuration
@EnableCaching
@EnableScheduling
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager("weather", "tour")
    }
}
```

**2. WeatherService.kt 캐시 적용**
```kotlin
@Service
class WeatherService(private val weatherApiClient: WeatherApiClient) {

    @Cacheable("weather", key = "#regionCode + '_' + #baseTime")
    fun getWeatherForecast(
        location: String?,
        regionCode: String?,
        baseTime: String?
    ): WeatherResponse {
        // 같은 파라미터로 호출 시 캐시에서 바로 리턴
        // API 호출 안함
    }

    @CacheEvict("weather", allEntries = true)
    @Scheduled(fixedRate = 43200000) // 12시간마다
    fun clearWeatherCache() {
        println("🗑️ 날씨 캐시 자동 삭제 완료")
    }
}
```

#### 캐시 동작 방식

1. **첫 번째 호출**: API 호출 → 결과 캐시 저장
2. **동일한 파라미터 재호출**: 캐시에서 바로 리턴 (API 호출 안함)
3. **12시간 후**: @Scheduled로 자동 캐시 삭제
4. **다음 호출**: 다시 API 호출하여 최신 데이터 캐시

#### 투어 도메인 캐시 구현 예시

```kotlin
@Service
class TourService(private val tourApiClient: TourApiClient) {

    @Cacheable("tour", key = "#areaCode + '_' + #contentType")
    fun getTourInfo(areaCode: String, contentType: String): TourResponse {
        // 관광청 API 호출
    }

    @CacheEvict("tour", allEntries = true)
    @Scheduled(fixedRate = 86400000) // 24시간마다
    fun clearTourCache() {
        println("🗑️ 관광 캐시 자동 삭제 완료")
    }
}
```

#### 캐시 설정 변경 방법

**캐시 이름 추가**:
```kotlin
return ConcurrentMapCacheManager("weather", "tour", "user") // "user" 추가
```

**캐시 키 전략 변경**:
```kotlin
@Cacheable("weather", key = "#location") // location만으로 키 생성
@Cacheable("weather", key = "T(java.time.LocalDate).now().toString() + '_' + #regionCode") // 날짜 포함
```

**수동 캐시 삭제**:
```kotlin
@CacheEvict("weather", key = "#regionCode + '_' + #baseTime") // 특정 키만 삭제
@CacheEvict("weather", allEntries = true) // 전체 삭제
```

### 환경 변수 설정

`.env` 파일:
```bash
# AI API
GROQ_API_KEY=your_groq_api_key

# 기상청/관광청 공통 API 키
WEATHER_API_KEY=your_api_key  # 관광청도 동일한 키 사용

# OAuth (구현 시 추가)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

### API 응답 형식

모든 API는 `ApiResponse`로 감싸기:
```kotlin
// 성공
return ApiResponse("성공", data)

// 실패 (예외 던지면 GlobalExceptionHandler가 자동 처리)
throw IllegalArgumentException("잘못된 파라미터")
```

### 새로운 Tool 추가 방법

```kotlin
// 1. Tool 클래스 생성
@Service
class YourTool(private val yourService: YourService) {
    @Tool(description = "설명")
    fun yourFunction(@ToolParam(description = "파라미터 설명") param: String): YourResponse {
        return yourService.processData(param)
    }
}

// 2. AiConfig에 등록
@Bean
fun chatClient(chatModel: ChatModel, weatherTool: WeatherTool, yourTool: YourTool): ChatClient {
    return ChatClient.builder(chatModel)
        .defaultTools(weatherTool, yourTool)  // 여기에 추가
        .build()
}
```

## 📚 API 문서

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API 명세서**: `docs/api-specification.yaml`
- **ERD**: `docs/erd-diagram.md`

## 🚀 실행 방법

```bash
./gradlew bootRun
```

