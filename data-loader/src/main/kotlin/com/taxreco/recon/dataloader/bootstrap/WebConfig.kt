package com.taxreco.recon.dataloader.bootstrap

import com.taxreco.recon.dataloader.security.SecurityTokenInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(private val securityTokenInterceptor: SecurityTokenInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(securityTokenInterceptor)
    }
}