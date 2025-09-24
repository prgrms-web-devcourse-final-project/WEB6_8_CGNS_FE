package com.back.koreaTravelGuide.application

// TODO: 메인 애플리케이션 클래스 - 스프링 부트 시작점 및 환경변수 로딩
import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.back.koreaTravelGuide"])
class KoreaTravelGuideApplication

fun main(args: Array<String>) {
    // .env 파일 로드
    val dotenv =
        dotenv {
            ignoreIfMissing = true
            ignoreIfMalformed = true
        }

    // 환경 변수를 시스템 프로퍼티로 설정
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }

    runApplication<KoreaTravelGuideApplication>(*args)
}
