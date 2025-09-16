# Spring AI 1.0.1 완벽 가이드 (2025년 9월 기준)

## 목차
1. [Spring AI란?](#spring-ai란)
2. [프로젝트 설정](#프로젝트-설정)
3. [Tool/Function Calling](#tool-function-calling)
4. [ChatClient 사용법](#chatclient-사용법)
5. [프롬프트 엔지니어링](#프롬프트-엔지니어링)
6. [모델별 설정](#모델별-설정)
7. [실전 예제](#실전-예제)

---

## Spring AI란?

Spring AI는 AI 애플리케이션 개발을 위한 Spring 프레임워크입니다. Python의 LangChain과 유사하지만 Spring의 강력한 기능과 통합됩니다.

### 주요 특징
- **다중 모델 지원**: OpenAI, Anthropic, Google, Ollama, Azure 등
- **Tool Calling**: AI가 외부 함수/API 호출 가능
- **벡터 DB 통합**: RAG(Retrieval Augmented Generation) 지원
- **스트리밍**: 실시간 응답 스트리밍

---

## 프로젝트 설정

### 1. build.gradle.kts 설정

```kotlin
plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

extra["springAiVersion"] = "1.0.1"

dependencies {
    // Spring Boot 기본
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Spring AI - OpenAI (1.0.1 정식 버전)
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // 환경 변수 관리
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}
```

### 2. application.yml 설정

```yaml
spring:
  ai:
    openai:
      api-key: ${GROQ_API_KEY}
      # Groq API 사용 (OpenAI 호환)
      base-url: https://api.groq.com/openai
      chat:
        options:
          model: openai/gpt-oss-120b  # Groq의 오픈소스 모델
          temperature: 0.7
          max-tokens: 4096

# 또는 OpenAI 직접 사용시:
# spring:
#   ai:
#     openai:
#       api-key: ${OPENAI_API_KEY}
#       chat:
#         options:
#           model: gpt-4o
#           temperature: 0.7
          
logging:
  level:
    org.springframework.ai: DEBUG
```

---

## Tool/Function Calling

### @Tool 어노테이션 방식 (권장)

```kotlin
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

@Service
class WeatherService {
    
    @Tool(description = "도시의 현재 날씨를 조회합니다")
    fun getWeather(
        @ToolParam(description = "도시 이름", required = true) 
        city: String,
        
        @ToolParam(description = "온도 단위 (celsius/fahrenheit)", required = false) 
        unit: String = "celsius"
    ): WeatherInfo {
        println("🔥 Tool Called: city=$city, unit=$unit")
        
        // API 호출 로직
        return WeatherInfo(
            city = city,
            temperature = 22,
            unit = unit,
            description = "맑음"
        )
    }
    
    data class WeatherInfo(
        val city: String,
        val temperature: Int,
        val unit: String,
        val description: String
    )
}
```

### Tool 등록 및 사용

```kotlin
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {
    
    @Bean
    fun chatClient(
        chatModel: ChatModel,
        weatherService: WeatherService  // @Tool이 있는 서비스 주입
    ): ChatClient {
        return ChatClient.builder(chatModel)
            .defaultTools(weatherService)  // Tool 자동 감지 및 등록
            .build()
    }
}
```

---

## ChatClient 사용법

### 기본 사용

```kotlin
@RestController
class ChatController(private val chatClient: ChatClient) {
    
    @GetMapping("/chat")
    fun chat(@RequestParam message: String): String {
        return chatClient.prompt()
            .user(message)
            .call()
            .content() ?: "응답 없음"
    }
}
```

### 스트리밍

```kotlin
@GetMapping("/stream")
fun stream(@RequestParam message: String): Flux<String> {
    return chatClient.prompt()
        .user(message)
        .stream()
        .content()
}
```

### 시스템 프롬프트 추가

```kotlin
@GetMapping("/chat-with-system")
fun chatWithSystem(@RequestParam message: String): String {
    return chatClient.prompt()
        .system("너는 친절한 AI 비서야. 항상 공손하게 대답해.")
        .user(message)
        .call()
        .content() ?: "응답 없음"
}
```

---

## 프롬프트 엔지니어링

### 프롬프트 템플릿

```kotlin
import org.springframework.ai.chat.prompt.PromptTemplate

@Service
class PromptService {
    
    fun generatePrompt(product: String, features: List<String>): String {
        val template = """
            제품: {product}
            특징: {features}
            
            위 제품의 마케팅 문구를 작성해주세요.
        """.trimIndent()
        
        val prompt = PromptTemplate(template)
        val variables = mapOf(
            "product" to product,
            "features" to features.joinToString(", ")
        )
        
        return prompt.render(variables)
    }
}
```

### 멀티모달 (이미지 + 텍스트)

```kotlin
@PostMapping("/analyze-image")
fun analyzeImage(
    @RequestParam image: MultipartFile,
    @RequestParam question: String
): String {
    val imageResource = ByteArrayResource(image.bytes)
    
    return chatClient.prompt()
        .user { userSpec ->
            userSpec.text(question)
            userSpec.media(MimeTypeUtils.IMAGE_PNG, imageResource)
        }
        .call()
        .content() ?: "분석 실패"
}
```

---

## 모델별 설정

### OpenAI

```kotlin
@Bean
fun openAiChatModel(): ChatModel {
    val api = OpenAiApi(System.getenv("OPENAI_API_KEY"))
    
    val options = OpenAiChatOptions.builder()
        .model("gpt-4o")
        .temperature(0.7)
        .maxTokens(4096)
        .build()
    
    return OpenAiChatModel(api, options)
}
```

### Groq (OpenAI 호환) - 우리 프로젝트 설정

```kotlin
// application.yml 설정만으로 간단하게 사용 가능
spring:
  ai:
    openai:
      api-key: ${GROQ_API_KEY}
      base-url: https://api.groq.com/openai  # /v1 제거 (자동 추가됨)
      chat:
        options:
          model: openai/gpt-oss-120b  # Groq의 오픈소스 모델
          temperature: 0.7
          max-tokens: 4096

# 또는 프로그래밍 방식
@Bean
fun groqChatModel(): ChatModel {
    val api = OpenAiApi(
        "https://api.groq.com/openai",  # /v1 제거
        System.getenv("GROQ_API_KEY")
    )

    val options = OpenAiChatOptions.builder()
        .model("openai/gpt-oss-120b")  # 오픈소스 모델
        .temperature(0.7)
        .maxTokens(4096)
        .build()

    return OpenAiChatModel(api, options)
}
```

### Anthropic Claude

```kotlin
dependencies {
    implementation("org.springframework.ai:spring-ai-anthropic-spring-boot-starter")
}

// application.yml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      chat:
        options:
          model: claude-3-opus-20240229
          max-tokens: 4096
```

---

## 실전 예제

### 실제 프로젝트 예제 (WeatherTool - 우리 프로젝트)

우리 프로젝트에서 구현한 실제 WeatherTool 예제입니다:

```kotlin
@Service
class WeatherTool(
    private val webClient: WebClient,
    @Value("\${weather.api.key}") private val serviceKey: String,
    @Value("\${weather.api.base-url}") private val apiUrl: String
) {

    @Tool(description = "중기 날씨 예보를 조회합니다. 3-10일 후의 날씨, 기온, 강수 확률 정보를 제공합니다.")
    fun getWeatherForecast(
        @ToolParam(description = "지역 이름 (예: 서울, 부산, 대구 등)", required = false) location: String?,
        @ToolParam(description = "지역 코드 (예: 11B10101)", required = false) regionCode: String?,
        @ToolParam(description = "발표 시각 (YYYYMMDDHHMM)", required = false) baseTime: String?
    ): WeatherResponse {

        val actualLocation = location ?: "서울"
        val actualRegionCode = regionCode ?: getRegionCodeFromLocation(actualLocation)
        val actualBaseTime = baseTime ?: getCurrentBaseTime()

        return try {
            // 3개 기상청 API 통합 호출
            val midForecastResponse = fetchMidForecast(actualRegionCode, actualBaseTime).block()
            val temperatureResponse = fetchTemperature(actualRegionCode, actualBaseTime).block()
            val landForecastResponse = fetchLandForecast(actualRegionCode, actualBaseTime).block()

            // 데이터 병합 및 처리
            val combinedForecast = combineWeatherData(
                midForecastText = midForecastResponse,
                temperatureData = temperatureResponse,
                landForecastData = landForecastResponse
            )

            WeatherResponse(
                region = actualLocation,
                regionCode = actualRegionCode,
                baseTime = actualBaseTime,
                forecast = combinedForecast.summary,
                details = combinedForecast.details
            )

        } catch (e: Exception) {
            WeatherResponse(
                region = actualLocation,
                regionCode = actualRegionCode,
                baseTime = actualBaseTime,
                forecast = "날씨 정보를 가져올 수 없습니다: \${e.message}",
                details = WeatherDetails()
            )
        }
    }

    // 3개 API 호출 메서드들
    private fun fetchMidForecast(regionId: String, baseTime: String): Mono<String?> { /* ... */ }
    private fun fetchTemperature(regionId: String, baseTime: String): Mono<TemperatureData?> { /* ... */ }
    private fun fetchLandForecast(regionId: String, baseTime: String): Mono<PrecipitationData?> { /* ... */ }
}
```

### 핵심 특징:
- **3개 API 통합**: 기상청 getMidFcst, getMidTa, getMidLandFcst 활용
- **지역 자동 변환**: "서울" → "11B10101" 지역코드 변환
- **시간 자동 계산**: 현재 시간 기준 최신 발표시각 사용
- **에러 처리**: API 실패시에도 안정적 응답 제공

### 간단한 날씨 조회 봇

```kotlin
@Service
class WeatherTool {
    
    @Tool(description = "도시의 날씨 정보를 조회합니다")
    fun getWeather(
        @ToolParam(description = "도시 이름") city: String
    ): Map<String, Any> {
        // 실제 API 호출
        val weatherData = callWeatherApi(city)
        
        return mapOf(
            "city" to city,
            "temperature" to weatherData.temp,
            "description" to weatherData.desc,
            "humidity" to weatherData.humidity
        )
    }
    
    private fun callWeatherApi(city: String): WeatherData {
        // WebClient로 외부 API 호출
        return WeatherData(25, "맑음", 60)
    }
    
    data class WeatherData(val temp: Int, val desc: String, val humidity: Int)
}

@RestController
class WeatherController(
    private val chatClient: ChatClient,
    private val weatherTool: WeatherTool
) {
    
    @PostMapping("/weather-chat")
    fun weatherChat(@RequestBody request: ChatRequest): ChatResponse {
        // Tool을 포함한 ChatClient 생성
        val response = chatClient.prompt()
            .user(request.message)
            .tools(weatherTool)  // Tool 등록
            .call()
            .content()
        
        return ChatResponse(response ?: "날씨 정보를 가져올 수 없습니다")
    }
    
    data class ChatRequest(val message: String)
    data class ChatResponse(val reply: String)
}
```

### RAG (Retrieval Augmented Generation)

```kotlin
@Service
class RagService(
    private val vectorStore: VectorStore,
    private val chatClient: ChatClient
) {
    
    fun queryWithContext(question: String): String {
        // 1. 벡터 DB에서 관련 문서 검색
        val relevantDocs = vectorStore.similaritySearch(
            SearchRequest.query(question).withTopK(5)
        )
        
        // 2. 컨텍스트 생성
        val context = relevantDocs.joinToString("\n") { it.content }
        
        // 3. AI에게 컨텍스트와 함께 질문
        val prompt = """
            다음 문서를 참고해서 질문에 답해주세요:
            
            문서:
            $context
            
            질문: $question
        """.trimIndent()
        
        return chatClient.prompt()
            .user(prompt)
            .call()
            .content() ?: "답변할 수 없습니다"
    }
}
```

---

## 트러블슈팅

### 1. Function Calling이 작동하지 않을 때

```kotlin
// 로깅 추가
@Tool(description = "테스트 함수")
fun testFunction(input: String): String {
    println("🔥 Function called with: $input")
    return "Result: $input"
}

// ChatClient 설정 확인
@Bean
fun chatClient(chatModel: ChatModel, toolService: ToolService): ChatClient {
    println("📌 Registering tools...")
    return ChatClient.builder(chatModel)
        .defaultTools(toolService)
        .build()
}
```

### 2. Groq API 사용시 주의사항 (우리 프로젝트 경험)

- **URL 중복 문제**: `base-url`에 `/v1` 포함하면 `/v1/v1/chat/completions` 오류 발생
  ```yaml
  # ❌ 잘못된 설정
  base-url: https://api.groq.com/openai/v1

  # ✅ 올바른 설정
  base-url: https://api.groq.com/openai
  ```
- **모델명**: `openai/gpt-oss-120b` 사용 (오픈소스 모델)
- **Tool Calling**: Groq에서 @Tool 기능 정상 작동 확인
- **응답 속도**: OpenAI보다 빠른 응답 속도

### 3. 메모리 관리

```kotlin
// 대화 히스토리 관리
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
class ConversationMemory {
    private val messages = mutableListOf<Message>()
    
    fun addMessage(role: String, content: String) {
        messages.add(Message(role, content))
        // 최대 10개 메시지만 유지
        if (messages.size > 10) {
            messages.removeAt(0)
        }
    }
    
    fun getHistory(): List<Message> = messages.toList()
}
```

---

## 참고 자료

- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)
- [Spring AI 1.0.1 릴리즈 노트](https://spring.io/blog/2025/05/20/spring-ai-1-0-GA-released/)
- [우리 프로젝트 GitHub](https://github.com/Mrbaeksang/spring-ai-weather-tool)

---

## 버전 정보

- **Spring AI**: 1.0.1 (2025년 5월 GA 릴리즈)
- **Spring Boot**: 3.5.5
- **Kotlin**: 1.9.25
- **Groq API**: openai/gpt-oss-120b 모델
- **기상청 API**: 중기예보조회서비스
- **작성일**: 2025년 9월

> ✅ **안정화**: Spring AI 1.0.1은 정식 GA(General Availability) 버전으로 프로덕션 환경에서 안전하게 사용 가능합니다.

## 실제 프로젝트 적용 사례

이 가이드의 모든 예제는 실제 동작하는 [spring-ai-weather-tool](https://github.com/Mrbaeksang/spring-ai-weather-tool) 프로젝트에서 검증되었습니다:

- ✅ **Spring AI 1.0.1 + Groq API** 연동 완료
- ✅ **@Tool 어노테이션** 기반 날씨 예보 서비스
- ✅ **3개 기상청 API 통합** (중기전망, 기온, 강수)
- ✅ **24개 지역 지원** 및 자동 지역코드 변환
- ✅ **에러 처리** 및 안정적인 응답 제공