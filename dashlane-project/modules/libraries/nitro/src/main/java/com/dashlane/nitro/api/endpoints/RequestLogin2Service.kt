package com.dashlane.nitro.api.endpoints

import com.dashlane.server.api.Response
import com.google.gson.annotations.SerializedName


interface RequestLogin2Service {

    companion object {
        const val URL = "/api/authentication/RequestLogin2"
    }

    suspend fun execute(request: Request): Response<Data>

    data class Request(
        @SerializedName("login")
        val login: String
    )

    data class Data(
        @SerializedName("domainName")
        val domainName: String,
        @SerializedName("idpAuthorizeUrl")
        val idpAuthorizeUrl: String,
        @SerializedName("spCallbackUrl")
        val spCallbackUrl: String,
        @SerializedName("teamUuid")
        val teamUuid: String
    )
}