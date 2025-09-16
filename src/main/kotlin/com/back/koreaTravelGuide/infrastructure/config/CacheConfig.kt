package com.back.koreaTravelGuide.infrastructure.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * 캐시 설정
 *
 * @Cacheable 어노테이션 기반 캐싱 활성화
 * - ConcurrentMapCacheManager: 인메모리 캐시 제공
 * - 각 도메인별로 캐시 이름 지정 가능
 * - 스케줄링을 통한 자동 캐시 갱신 지원
 *
 * 사용법:
 * @Cacheable("weather") 어노테이션으로 메서드 결과 캐싱
 * @CacheEvict("weather", allEntries = true)로 캐시 삭제
 */
@Configuration
@EnableCaching
@EnableScheduling
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager("weather", "tour")
    }
}