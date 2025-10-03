package com.back.koreaTravelGuide.config

import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory

@TestConfiguration
class TestConfig {
    @Bean
    @Primary
    fun redisConnectionFactory(): RedisConnectionFactory = Mockito.mock(RedisConnectionFactory::class.java)
}
