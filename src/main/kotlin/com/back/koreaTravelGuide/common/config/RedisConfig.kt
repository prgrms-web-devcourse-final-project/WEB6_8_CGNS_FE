package com.back.koreaTravelGuide.common.config

import com.back.koreaTravelGuide.domain.ai.tour.dto.TourDetailResponse
import com.back.koreaTravelGuide.domain.ai.tour.dto.TourResponse
import com.back.koreaTravelGuide.domain.ai.weather.dto.MidForecastDto
import com.back.koreaTravelGuide.domain.ai.weather.dto.TemperatureAndLandForecastDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host:localhost}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port:6379}")
    private var redisPort: Int = 6379

    @Value("\${spring.data.redis.password:}")
    private var redisPassword: String = ""

    @Bean
    @ConditionalOnMissingBean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisConfiguration = RedisStandaloneConfiguration()
        redisConfiguration.hostName = redisHost
        redisConfiguration.port = redisPort
        if (redisPassword.isNotEmpty()) {
            redisConfiguration.setPassword(redisPassword)
        }
        return LettuceConnectionFactory(redisConfiguration)
    }

    @Bean
    fun objectMapper(): ObjectMapper =
        ObjectMapper().apply {
            // Kotlin 모듈 등록 (data class 생성자 인식)
            registerModule(KotlinModule.Builder().build())
        }

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
    fun cacheManager(
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper,
    ): CacheManager {
        // 각 캐시 타입별 Serializer 생성
        val tourResponseSerializer = Jackson2JsonRedisSerializer<TourResponse>(objectMapper, TourResponse::class.java)
        val tourDetailResponseSerializer = Jackson2JsonRedisSerializer<TourDetailResponse>(objectMapper, TourDetailResponse::class.java)

        // List<MidForecastDto> 타입을 위한 JavaType 생성
        val midForecastListType =
            objectMapper.typeFactory.constructCollectionType(
                List::class.java,
                MidForecastDto::class.java,
            )
        val midForecastListSerializer = Jackson2JsonRedisSerializer<List<MidForecastDto>>(objectMapper, midForecastListType)

        // List<TemperatureAndLandForecastDto> 타입을 위한 JavaType 생성
        val tempAndLandListType =
            objectMapper.typeFactory.constructCollectionType(
                List::class.java,
                TemperatureAndLandForecastDto::class.java,
            )
        val tempAndLandListSerializer = Jackson2JsonRedisSerializer<List<TemperatureAndLandForecastDto>>(objectMapper, tempAndLandListType)

        // 공통 키 Serializer
        val keySerializer = RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())

        // Tour 관련 캐시 설정 (TourResponse 타입)
        val tourResponseConfig =
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keySerializer)
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(tourResponseSerializer),
                )
                .entryTtl(Duration.ofHours(12))

        // Tour 상세 캐시 설정 (TourDetailResponse 타입)
        val tourDetailConfig =
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keySerializer)
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(tourDetailResponseSerializer),
                )
                .entryTtl(Duration.ofHours(12))

        // 중기예보 캐시 설정 (List<MidForecastDto> 타입)
        val midForecastConfig =
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keySerializer)
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(midForecastListSerializer),
                )
                .entryTtl(Duration.ofHours(12))

        // 기온/육상 예보 캐시 설정 (List<TemperatureAndLandForecastDto> 타입)
        val tempAndLandConfig =
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keySerializer)
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(tempAndLandListSerializer),
                )
                .entryTtl(Duration.ofHours(12))

        return RedisCacheManager.builder(connectionFactory)
            .withCacheConfiguration("tourAreaBased", tourResponseConfig)
            .withCacheConfiguration("tourLocationBased", tourResponseConfig)
            .withCacheConfiguration("tourDetail", tourDetailConfig)
            .withCacheConfiguration("weatherMidFore", midForecastConfig)
            .withCacheConfiguration("weatherTempAndLandFore", tempAndLandConfig)
            .build()
    }
}
