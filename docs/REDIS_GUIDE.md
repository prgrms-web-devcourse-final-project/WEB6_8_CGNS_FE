# 🔴 Redis 사용 가이드

## 🚀 빠른 시작

### 1. Redis 서버 실행 (Docker 추천)
```bash
# Redis 서버 시작 (비밀번호 설정 + 데이터 영속화) - 한 줄 복붙
docker run -d --name redis --restart unless-stopped -p 6379:6379 -e TZ=Asia/Seoul -v redis_data:/data redis:alpine --requirepass 'your_password_here'

# 간단 버전 (비밀번호 없음)
docker run -d -p 6379:6379 --name redis redis:alpine

# Redis 중지
docker stop redis

# Redis 재시작
docker start redis

# Redis 로그 확인
docker logs redis

# Redis 완전 삭제 (데이터 포함)
docker rm -f redis && docker volume rm redis_data
```

### 2. 환경변수 설정
```bash
# .env 파일 생성 (.env.example 복사)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_password_here  # 비밀번호 설정한 경우
# REDIS_PASSWORD=                  # 비밀번호 없으면 빈 값
```

## 💾 캐시 사용법

### @Cacheable - 데이터 캐싱
```kotlin
@Service
class WeatherService {

    // 30분간 캐시 (CacheConfig.kt에서 설정)
    @Cacheable("weather")
    fun getWeatherData(location: String): WeatherData {
        // API 호출은 캐시가 없을 때만 실행됨
        return callWeatherApi(location)
    }
}
```

### @CacheEvict - 캐시 삭제
```kotlin
@Service
class WeatherService {

    // 특정 키 캐시 삭제
    @CacheEvict(value = "weather", key = "#location")
    fun updateWeatherData(location: String) {
        // 캐시 삭제 후 새로운 데이터로 갱신
    }

    // 전체 캐시 삭제
    @CacheEvict(value = "weather", allEntries = true)
    fun clearAllWeatherCache() {
        // 모든 날씨 캐시 삭제
    }
}
```

## 🎯 실제 사용 예시

### 날씨 API 캐싱 (30분)
```kotlin
@Cacheable("weather", key = "#location")
fun getMidTermForecast(location: String): MidTermForecast {
    // 기상청 API 호출 (30분간 캐시됨)
    return weatherApiClient.getMidTermForecast(location)
}
```

### 관광 정보 캐싱 (1시간)
```kotlin
@Cacheable("tour", key = "#area")
fun getTourSpots(area: String): List<TourSpot> {
    // 관광 API 호출 (1시간 캐시됨)
    return tourApiClient.getTourSpots(area)
}
```

## ⚙️ 설정 파일

### CacheConfig.kt에서 TTL 설정
```kotlin
@Bean
fun cacheManager(): RedisCacheManager {
    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 기본 30분
        )
        .withCacheConfiguration("weather",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 날씨: 30분
        )
        .withCacheConfiguration("tour",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))     // 관광: 1시간
        )
        .build()
}
```

## 🔧 개발/운영 분리

### 개발용: Redis 없이 실행
```yaml
# application.yml
spring:
  session:
    store-type: none  # Redis 없어도 실행됨
```

### 운영용: Redis 세션 저장
```yaml
# application-prod.yml
spring:
  session:
    store-type: redis  # 세션을 Redis에 저장
```

## 📊 모니터링

### Health Check
```
GET http://localhost:8080/actuator/health
```
- `redis.status: UP` = 정상
- `redis.status: DOWN` = Redis 서버 확인 필요

### Redis CLI 접속
```bash
# Docker 컨테이너 접속 (비밀번호 없는 경우)
docker exec -it redis redis-cli

# Docker 컨테이너 접속 (비밀번호 있는 경우)
docker exec -it redis redis-cli -a 'your_password_here'

# 또는 접속 후 인증
docker exec -it redis redis-cli
> AUTH your_password_here

# 캐시 확인
> KEYS *
> GET "weather::서울"
> TTL "weather::서울"  # 남은 시간 확인 (초 단위)
> DEL "weather::서울"  # 특정 캐시 삭제
> FLUSHALL  # 모든 캐시 삭제
```

## 🚨 주의사항

1. **개발용**: Redis 없어도 실행됨 (`session.store-type: none`)
2. **비밀번호**: 운영 환경에서는 반드시 강력한 비밀번호 설정 권장
3. **데이터 영속화**: `-v redis_data:/data` 옵션으로 컨테이너 재시작 시에도 데이터 보존
4. **캐시 키**: 특수문자 주의 (`::` 구분자 사용)
5. **TTL**: 적절한 캐시 시간 설정 (API 호출량 고려)
6. **메모리**: Redis 메모리 사용량 모니터링 필요