package com.dashlane.nitro.api.tunnel.endpoints

import com.dashlane.server.api.DashlaneApiClient
import com.dashlane.server.api.Response
import com.dashlane.server.api.execute

internal class ClientHelloServiceImpl(
    private val client: DashlaneApiClient
) : ClientHelloService {
    override suspend fun execute(
        request: ClientHelloService.Request
    ): Response<ClientHelloService.Data> = client.execute(
        path = "/api/tunnel/ClientHello",
        request = request,
        businessExceptions = emptyMap()
    )
}