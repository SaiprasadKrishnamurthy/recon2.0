package com.taxreco.recon.dataloader.security

import com.taxreco.recon.dataloader.model.ApiUser
import com.taxreco.recon.dataloader.repository.ApiUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SecurityTokenInterceptor(
    private val apiUser: ApiUser,
    private val apiUserRepository: ApiUserRepository

) : HandlerInterceptor {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        try {
            if (isURLSecured(request)) {
                val clientId = request.getHeader("X-CLIENT-ID")
                val apiKey = request.getHeader("X-API-KEY")
                val existing = apiUserRepository.findByClientIdAndApiKey(clientId, apiKey)
                if (existing != null) {
                    apiUser.id = existing.id
                    apiUser.tenant = existing.tenant
                    apiUser.objectStorageId = existing.objectStorageId
                } else {
                    logger.error("Invalid credentials")
                    response.status = HttpServletResponse.SC_FORBIDDEN
                }
            }
        } catch (e: Exception) {
            logger.error("Error ",e)
            throw SecurityException()
//            response.status = HttpServletResponse.SC_FORBIDDEN
        }
        return super.preHandle(request, response, handler)
    }

    private fun isURLSecured(request: HttpServletRequest): Boolean {
        return request.requestURI.contains("/api/") && !request.requestURI.contains("/api/v1/login")
                && !request.requestURI.contains("/api/v1/logout") &&
                !request.requestURI.contains("/api/v1/generate-reset-password-link") &&
                !request.requestURI.contains("/api/v1/generate-registration-link")
                && !request.requestURI.contains("/api/v1/config")
                && !request.requestURI.contains("/api/v1/change-password")
                && !request.requestURI.contains("/api/v1/verify-reset-password-link")
                && !request.requestURI.contains("/api/v1/verify-registration-link")
                && !request.requestURI.contains("/api/v1/register")
                && !request.requestURI.contains("/api/v1/check-user-session")
                && !request.requestURI.contains("/api/v1/check-user-exist")
                && !request.requestURI.contains("/api/v1/updateLastLogin")

    }
}
