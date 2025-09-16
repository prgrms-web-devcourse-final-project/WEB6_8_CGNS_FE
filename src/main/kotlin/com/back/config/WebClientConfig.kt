package com.back.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.codec.xml.Jaxb2XmlDecoder
import org.springframework.http.codec.xml.Jaxb2XmlEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun webClient(): WebClient {
        // XML을 처리할 수 있는 XmlMapper 설정
        val xmlMapper = XmlMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        }

        // JSON과 XML을 모두 처리할 수 있는 ExchangeStrategies 설정
        val strategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                // 메모리 제한 설정 (10MB)
                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)

                // XML 디코더 설정
                configurer.defaultCodecs().jaxb2Decoder(Jaxb2XmlDecoder())
                configurer.defaultCodecs().jaxb2Encoder(Jaxb2XmlEncoder())
            }
            .build()

        return WebClient.builder()
            .exchangeStrategies(strategies)
            .build()
    }
}