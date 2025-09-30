plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
    // 코틀린 코드 스타일 린터
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    // 빌드 설정 상수 생성
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

group = "com.back"
version = "0.0.1-SNAPSHOT"
description = "backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Spring AI - 1.1.0-M2 (최신 버전) - 새로운 artifact명
    implementation("org.springframework.ai:spring-ai-starter-model-openai:1.1.0-M2")
    // Spring AI - JDBC 채팅 메모리 저장소 (PostgreSQL 대화 기록 저장)
    implementation("org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc:1.1.0-M2")

    // 환경 변수 관리 라이브러리
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // 보안: CVE 취약점 방지를 위한 최신 버전 강제
    implementation("org.apache.commons:commons-lang3:3.18.0") // CVE-2025-48924 해결

    // 웹소켓 - 사용자 채팅 기능 (게스트-가이드)
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // 레디스 - 캐싱 및 세션 관리
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")

    // API 문서화 - Swagger UI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    // 개발 도구 - 5명 팀 개발용
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // 모니터링 및 상태 체크 (개발자 디버깅용)
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // 데이터베이스 지원
    runtimeOnly("com.h2database:h2") // 개발용
    runtimeOnly("org.postgresql:postgresql") // 운영용
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// BOM 제거: Spring AI 1.1.0-M2에서 직접 버전 관리
// dependencyManagement 제거로 더 명확한 의존성 관리

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 코틀린 코드 스타일 린터 설정
ktlint {
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    // additionalEditorconfigFile.set(file(".editorconfig")) // .editorconfig 파일이 있다면 주석 해제
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/generated/**")
        exclude("**/BuildConfig.kt")
        include("**/kotlin/**")
    }
}

buildConfig {
    useKotlinOutput()

    val regionCodes =
        file("src/main/resources/region-codes.yml")
            .readText()
            .substringAfter("codes:")
            .lines()
            .filter { it.contains(":") }
            .joinToString(", ") { line ->
                val parts = line.split(":")
                "${parts[0].trim()}: ${parts[1].trim().replace("\"", "")}"
            }

    val promptsText = file("src/main/resources/prompts.yml").readText()
    val systemPrompt =
        promptsText
            .substringAfter("korea-travel-guide: |")
            .substringBefore("  errors:")
            .trim()

    val errorPrompt =
        promptsText
            .substringAfter("ai-fallback: \"")
            .substringBefore("\"")

    buildConfigField("String", "REGION_CODES_DESCRIPTION", "\"\"\"$regionCodes\"\"\"")
    buildConfigField("String", "KOREA_TRAVEL_GUIDE_SYSTEM", "\"\"\"$systemPrompt\"\"\"")
    buildConfigField("String", "AI_ERROR_FALLBACK", "\"\"\"$errorPrompt\"\"\"")
}
