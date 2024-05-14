package com.dashlane.nitro.api.endpoints

import com.dashlane.server.api.Response
import com.google.gson.annotations.SerializedName

interface ConfirmLogin2Service {

    companion object {
        const val URL = "/api/authentication/ConfirmLogin2"
    }

    suspend fun execute(request: Request): Response<Data>

    data class Request(
        @SerializedName("teamUuid")
        val teamUuid: String,
        @SerializedName("domainName")
        val domainName: String,
        @SerializedName("samlResponse")
        val samlResponse: String
    )

    data class Data(
        @SerializedName("ssoToken")
        val ssoToken: String,
        @SerializedName("userServiceProviderKey")
        val userServiceProviderKey: String,
        @SerializedName("exists")
        val exists: Boolean,
        @SerializedName("currentAuthenticationMethods")
        val currentAuthenticationMethods: List<String>,
        @SerializedName("expectedAuthenticationMethods")
        val expectedAuthenticationMethods: List<String>
    )
}