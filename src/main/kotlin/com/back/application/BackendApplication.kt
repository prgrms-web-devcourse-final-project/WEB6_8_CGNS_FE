package com.back.application

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.back"])
class BackendApplication

fun main(args: Array<String>) {
    // .env 파일 로드
    val dotenv = dotenv {
        ignoreIfMissing = true
        ignoreIfMalformed = true
    }

    // 시스템 프로퍼티로 설정 (Spring이 인식 가능)
    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }

    runApplication<BackendApplication>(*args)
}