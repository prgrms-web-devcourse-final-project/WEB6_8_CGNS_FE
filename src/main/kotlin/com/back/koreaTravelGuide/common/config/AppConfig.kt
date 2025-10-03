package com.back.koreaTravelGuide.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig(
    @Value("\${custom.site.cookieDomain}") cookieDomain: String,
    @Value("\${custom.site.frontUrl}") siteFrontUrl: String,
    @Value("\${custom.site.backUrl}") siteBackUrl: String,
) {
    init {
        _cookieDomain = cookieDomain
        _siteFrontUrl = siteFrontUrl
        _siteBackUrl = siteBackUrl
    }

    companion object {
        private lateinit var _cookieDomain: String
        private lateinit var _siteFrontUrl: String
        private lateinit var _siteBackUrl: String

        val cookieDomain: String by lazy { _cookieDomain }
        val siteFrontUrl: String by lazy { _siteFrontUrl }
        val siteBackUrl: String by lazy { _siteBackUrl }
    }
}
