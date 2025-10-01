package com.back.koreaTravelGuide.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()

        template.connectionFactory = connectionFactory

// Key와 Value의 Serializer를 String으로 설정

        template.keySerializer = StringRedisSerializer()

        template.valueSerializer = StringRedisSerializer()

        return template
    }

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        // Kotlin data class 역직렬화 지원을 위한 ObjectMapper 생성
        val objectMapper =
            ObjectMapper().apply {
                // Kotlin 모듈 등록 (data class 생성자 인식)
                registerModule(KotlinModule.Builder().build())
                // 타입 정보 보존을 위한 검증기 설정
                activateDefaultTyping(
                    BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Any::class.java)
                        .build(),
                    ObjectMapper.DefaultTyping.NON_FINAL,
                )
            }

        val redisCacheConfiguration =
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()),
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        GenericJackson2JsonRedisSerializer(objectMapper),
                    ),
                )
                .entryTtl(Duration.ofHours(12))

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(redisCacheConfiguration)
            .build()
    }
}
