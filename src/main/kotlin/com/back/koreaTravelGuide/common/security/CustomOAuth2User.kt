package com.back.koreaTravelGuide.common.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

class CustomOAuth2User(
    val id: Long,
    val email: String,
    authorities: Collection<GrantedAuthority>,
    attributes: Map<String, Any>,
    val nameAttributeKey: String,
) : DefaultOAuth2User(authorities, attributes, nameAttributeKey) {
    override fun getName(): String {
        val nameAttribute = getAttribute<Any>(nameAttributeKey)

        if (nameAttribute is Map<*, *>) {
            return nameAttribute["id"] as String
        }

        return nameAttribute.toString()
    }
}
