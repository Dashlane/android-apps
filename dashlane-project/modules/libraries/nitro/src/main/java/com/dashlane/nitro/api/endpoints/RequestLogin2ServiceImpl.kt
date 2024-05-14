package com.dashlane.nitro.api.endpoints

import com.dashlane.nitro.api.NitroApiClient
import com.dashlane.nitro.api.execute
import com.dashlane.server.api.Response

internal class RequestLogin2ServiceImpl(
    private val client: NitroApiClient
) : RequestLogin2Service {
    override suspend fun execute(
        request: RequestLogin2Service.Request
    ): Response<RequestLogin2Service.Data> = client.execute(
        path = RequestLogin2Service.URL,
        request = request,
        businessExceptions = emptyMap()
    )
}