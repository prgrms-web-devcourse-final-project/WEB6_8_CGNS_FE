package com.back.koreaTravelGuide

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
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
