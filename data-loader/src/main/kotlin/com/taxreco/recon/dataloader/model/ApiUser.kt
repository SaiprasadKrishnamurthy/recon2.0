package com.taxreco.recon.dataloader.model

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component

@Component
@Scope("request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class ApiUser {
    var id: String = ""
    var tenant: String = ""
    var objectStorageId: String = ""

    fun clone(): ApiUser {
        val user = ApiUser()
        user.id = this.id
        user.tenant = this.tenant
        user.objectStorageId = this.objectStorageId
        return user
    }

}