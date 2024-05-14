package com.dashlane.nitro.api.tunnel.endpoints

import com.dashlane.server.api.DashlaneApiClient
import com.dashlane.server.api.Response
import com.dashlane.server.api.execute

internal class TerminateHelloServiceImpl(
    private val client: DashlaneApiClient
) : TerminateHelloService {
    override suspend fun execute(
        request: TerminateHelloService.Request
    ): Response<Unit> = client.execute(
        path = "/api/tunnel/TerminateHello",
        request = request,
        businessExceptions = emptyMap()
    )
}