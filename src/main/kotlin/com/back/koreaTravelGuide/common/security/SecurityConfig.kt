package com.back.koreaTravelGuide.common.config

import com.back.koreaTravelGuide.security.CustomOAuth2LoginSuccessHandler
import com.back.koreaTravelGuide.security.CustomOAuth2UserService
import com.back.koreaTravelGuide.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            // 기본 보안 기능
            csrf { disable() }
            formLogin { disable() }
            httpBasic { disable() }
            logout { disable() }

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            oauth2Login {
                userInfoEndpoint {
                    userService = customOAuth2UserService
                }
                authenticationSuccessHandler = customOAuth2LoginSuccessHandler
            }

            authorizeHttpRequests {
                authorize("/api/auth/**", permitAll)
                authorize("/swagger-ui/**", "/v3/api-docs/**", permitAll)
                authorize("/h2-console/**", permitAll)
                authorize("/favicon.ico", permitAll)
                authorize(anyRequest, authenticated)
            }
            addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        }

        return http.build()
    }
}
