package com.back.koreaTravelGuide.domain.weather.service

// TODO: 날씨 정보 캐싱 서비스 - 12시간 주기 캐시 관리 및 데이터 제공
import com.back.koreaTravelGuide.domain.weather.dto.WeatherResponse
import org.springframework.stereotype.Service

@Service
class WeatherService(
    private val weatherServiceCore: WeatherServiceCore
) {
    
    fun getWeatherForecast(
        location: String?,
        regionCode: String?,
        baseTime: String?
    ): WeatherResponse {
        // 여기에 캐싱 로직 추가 가능
        return weatherServiceCore.getWeatherForecast(location, regionCode, baseTime)
    }
}