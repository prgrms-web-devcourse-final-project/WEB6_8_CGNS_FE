package com.back.koreaTravelGuide.common.security

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
                "naver" -> parseNaver(attributes)
                "kakao" -> parseKakao(attributes)
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

        val userNameAttributeName = userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        return CustomOAuth2User(
            id = user.id!!,
            email = user.email,
            authorities = authorities,
            attributes = attributes,
            nameAttributeKey = userNameAttributeName,
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

    private fun parseNaver(attributes: Map<String, Any>): OAuthUserInfo {
        val response = attributes["response"] as Map<String, Any>

        return OAuthUserInfo(
            oauthId = response["id"] as String,
            email = response["email"] as String,
            nickname = response["name"] as String,
            profileImageUrl = response["profile_image"] as String?,
        )
    }

    private fun parseKakao(attributes: Map<String, Any>): OAuthUserInfo {
        val kakaoAccount = attributes["kakao_account"] as? Map<String, Any>
        val profile = kakaoAccount?.get("profile") as? Map<String, Any>
        val kakaoId = attributes["id"].toString()

        // 카카오는 이메일 못받아서 이렇게 처리했음
        val email = kakaoAccount?.get("email") as? String ?: "kakao_$kakaoId@social.login"

        return OAuthUserInfo(
            oauthId = kakaoId,
            email = email,
            nickname = profile?.get("nickname") as? String ?: "사용자",
            profileImageUrl = profile?.get("profile_image_url") as? String,
        )
    }
}

data class OAuthUserInfo(
    val oauthId: String,
    val email: String,
    val nickname: String,
    val profileImageUrl: String?,
)
