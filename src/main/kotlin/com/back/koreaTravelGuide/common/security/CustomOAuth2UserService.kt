package com.back.koreaTravelGuide.security

import com.back.koreaTravelGuide.domain.user.entity.User
import com.back.koreaTravelGuide.domain.user.enums.UserRole
import com.back.koreaTravelGuide.domain.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
) : DefaultOAuth2UserService() {
    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId
        val attributes = oAuth2User.attributes

        val oAuthUserInfo =
            when (provider) {
                "google" -> parseGoogle(attributes)
                else -> throw IllegalArgumentException("지원하지 않는 소셜 로그인입니다.")
            }

        val user =
            userRepository.findByOauthProviderAndOauthId(provider, oAuthUserInfo.oauthId)
                ?: userRepository.save(
                    User(
                        oauthProvider = provider,
                        oauthId = oAuthUserInfo.oauthId,
                        email = oAuthUserInfo.email,
                        nickname = oAuthUserInfo.nickname,
                        profileImageUrl = oAuthUserInfo.profileImageUrl,
                        role = UserRole.PENDING,
                    ),
                )

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

        return CustomOAuth2User(
            id = user.id!!,
            email = user.email,
            authorities = authorities,
            attributes = attributes,
        )
    }

    private fun parseGoogle(attributes: Map<String, Any>): OAuthUserInfo {
        return OAuthUserInfo(
            oauthId = attributes["sub"] as String,
            email = attributes["email"] as String,
            nickname = attributes["name"] as String,
            profileImageUrl = attributes["picture"] as String?,
        )
    }
}

data class OAuthUserInfo(
    val oauthId: String,
    val email: String,
    val nickname: String,
    val profileImageUrl: String?,
)
