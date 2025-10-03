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
        val isDev =
            environment.getProperty("spring.profiles.active")?.contains("dev") == true ||
                environment.activeProfiles.contains("dev")

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

            if (!isDev) {
                oauth2Login {
                    userInfoEndpoint {
                        userService = customOAuth2UserService
                    }
                    authenticationSuccessHandler = customOAuth2LoginSuccessHandler
                }
            }

            authorizeHttpRequests {
                authorize("/h2-console/**", permitAll)
                authorize("/swagger-ui/**", "/v3/api-docs/**", permitAll)
                authorize("/api/auth/**", permitAll)
                authorize("/favicon.ico", permitAll)
                if (isDev) {
                    authorize(anyRequest, permitAll)
                } else {
                    authorize(anyRequest, authenticated)
                }
            }

            if (!isDev) {
                addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
            }
        }

        return http.build()
    }
}
