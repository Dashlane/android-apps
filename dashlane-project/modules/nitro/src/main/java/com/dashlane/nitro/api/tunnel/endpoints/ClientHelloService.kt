package com.dashlane.nitro.api.tunnel.endpoints

import com.dashlane.server.api.Response
import com.google.gson.annotations.SerializedName



internal interface ClientHelloService {
    

    suspend fun execute(request: Request): Response<Data>

    

    data class Request(
        @SerializedName("clientPublicKey")
        val clientPublicKey: String
    )

    

    data class Data(
        @SerializedName("attestation")
        val attestation: String
    )
}