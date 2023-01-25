package com.dashlane.nitro.api.endpoints

import com.dashlane.nitro.api.NitroApiClient
import com.dashlane.nitro.api.execute
import com.dashlane.server.api.Response

internal class RequestLoginServiceImpl(
    private val client: NitroApiClient
) : RequestLoginService {
    override suspend fun execute(
        request: RequestLoginService.Request
    ): Response<RequestLoginService.Data> = client.execute(
        path = "/api/authentication/RequestLogin",
        request = request,
        businessExceptions = emptyMap()
    )
}