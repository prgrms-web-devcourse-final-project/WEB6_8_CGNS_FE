package com.back.koreaTravelGuide.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

class CustomOAuth2User(
    val id: Long,
    val email: String,
    authorities: Collection<GrantedAuthority>,
    attributes: Map<String, Any>,
) : DefaultOAuth2User(authorities, attributes, "email")
