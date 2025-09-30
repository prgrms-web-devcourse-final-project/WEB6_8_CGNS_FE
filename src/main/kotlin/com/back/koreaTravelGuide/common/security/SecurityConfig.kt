package com.back.koreaTravelGuide.common.config

import com.back.koreaTravelGuide.common.security.CustomOAuth2LoginSuccessHandler
import com.back.koreaTravelGuide.common.security.CustomOAuth2UserService
import com.back.koreaTravelGuide.common.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val customOAuth2LoginSuccessHandler: CustomOAuth2LoginSuccessHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val environment: Environment,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val isDev = environment.activeProfiles.contains("dev")

        http {
            csrf { disable() }
            formLogin { disable() }
            httpBasic { disable() }
            logout { disable() }

            headers {
                if (isDev) {
                    frameOptions { disable() }
                } else {
                    frameOptions { sameOrigin }
                }
            }

            sessionManagement {
                sessionCreationPolicy =
                    if (isDev) {
                        SessionCreationPolicy.IF_REQUIRED
                    } else {
                        SessionCreationPolicy.STATELESS
                    }
            }

            oauth2Login {
                userInfoEndpoint {
                    userService = customOAuth2UserService
                }
                authenticationSuccessHandler = customOAuth2LoginSuccessHandler
            }

            authorizeHttpRequests {
                // 인증 없이 접근을 허용할 경로들
                authorize("/h2-console/**", permitAll)
                authorize("/swagger-ui/**", "/v3/api-docs/**", permitAll)
                authorize("/api/auth/**", permitAll) // 토큰 재발급 API
                authorize("/favicon.ico", permitAll)

                // 소셜 로그인 흐름을 위한 경로 허용
                authorize("/login/oauth2/code/*", permitAll)
                authorize("/oauth2/authorization/*", permitAll)

                // 위에서 허용한 경로 외의 모든 요청은 인증 필요
                authorize(anyRequest, authenticated)
            }

            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        }

        return http.build()
    }
}
