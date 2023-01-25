package com.dashlane.nitro.api.endpoints

import com.dashlane.server.api.Response
import com.google.gson.annotations.SerializedName



interface RequestLoginService {
    

    suspend fun execute(request: Request): Response<Data>

    data class Request(
        @SerializedName("domainName")
        val domainName: String
    )

    data class Data(
        @SerializedName("idpAuthorizeUrl")
        val idpAuthorizeUrl: String,
        @SerializedName("spCallbackUrl")
        val spCallbackUrl: String
    )
}