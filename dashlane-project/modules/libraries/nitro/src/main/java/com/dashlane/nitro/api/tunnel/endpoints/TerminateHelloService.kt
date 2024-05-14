package com.dashlane.nitro.api.tunnel.endpoints

import com.dashlane.server.api.Response
import com.google.gson.annotations.SerializedName

internal interface TerminateHelloService {
    suspend fun execute(request: Request): Response<Unit>

    data class Request(
        @SerializedName("clientHeader")
        val clientHeader: String
    )
}