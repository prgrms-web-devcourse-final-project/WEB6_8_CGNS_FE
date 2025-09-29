package com.back.koreaTravelGuide.domain.ai.weather.cache

import org.springframework.context.annotation.Configuration

@Configuration
class WeatherCacheConfig {
    //    @Bean
//    fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
//        val config = RedisCacheConfiguration.defaultCacheConfig()
//            .entryTtl(Duration.ofHours(12))
//            .disableCachingNullValues()
//
//        return RedisCacheManager.builder(connectionFactory)
//            .cacheDefaults(config)
//            .build()
//    }
}
