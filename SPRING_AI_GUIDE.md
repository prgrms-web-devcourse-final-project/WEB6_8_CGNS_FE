# Spring AI 완벽 가이드 (2025년 9월 기준)

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

extra["springAiVersion"] = "1.0.0-M5"

dependencies {
    // Spring Boot 기본
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // Spring AI - OpenAI
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    
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
      api-key: ${OPENAI_API_KEY}
      # Groq 사용시
      # base-url: https://api.groq.com/openai/v1
      chat:
        options:
          model: gpt-4o  # 또는 gpt-3.5-turbo
          temperature: 0.7
          max-tokens: 4096
          
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

### Groq (OpenAI 호환)

```kotlin
@Bean
fun groqChatModel(): ChatModel {
    val api = OpenAiApi(
        "https://api.groq.com/openai/v1",
        System.getenv("GROQ_API_KEY")
    )
    
    val options = OpenAiChatOptions.builder()
        .model("llama-3.3-70b-versatile")  // Groq 모델
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

### 날씨 조회 봇

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

### 2. Groq API 사용시 주의사항

- Function Calling 지원 여부 확인 필요
- 일부 모델은 Tool Calling 미지원
- OpenAI 호환 엔드포인트 사용: `https://api.groq.com/openai/v1`

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
- [Spring AI 1.0.0-M5 릴리즈 노트](https://spring.io/blog/2024/12/23/spring-ai-1-0-0-m5-released/)

---

## 버전 정보

- **Spring AI**: 1.0.0-M5 (2024년 12월 릴리즈)
- **Spring Boot**: 3.5.5
- **Kotlin**: 1.9.25
- **작성일**: 2025년 9월

> ⚠️ **주의**: Spring AI는 아직 마일스톤 버전입니다. GA(General Availability) 버전은 2025년 초 예정입니다.