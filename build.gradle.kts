plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
	// ktlint plugin
	id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
	// detekt plugin
	id("io.gitlab.arturbosch.detekt") version "1.23.4"
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

extra["springAiVersion"] = "1.0.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	
	// Spring AI
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
	
	// XML parsing for weather API
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
	implementation("org.springframework:spring-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations")
	
	// Dotenv for environment variables
	implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
	
	runtimeOnly("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

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

// ktlint configuration
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
		include("**/kotlin/**")
	}
}

// detekt configuration
detekt {
	config = files(".config/detekt/detekt.yml") // detekt 설정 파일 필요 (선택사항)
	buildUponDefaultConfig = true
	allRules = false
	ignoreFailures = false
	parallel = true
	autoCorrect = true // detekt가 일부 문제를 자동 수정하도록 허용
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
	reports {
		html.required.set(true)
		xml.required.set(true)
		txt.required.set(true)
		sarif.required.set(true)
	}
}