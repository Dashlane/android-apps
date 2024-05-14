package com.dashlane.nitro.api.endpoints

import com.dashlane.nitro.api.NitroApiClient
import com.dashlane.nitro.api.execute
import com.dashlane.server.api.Response

internal class ConfirmLogin2ServiceImpl(
    private val client: NitroApiClient
) : ConfirmLogin2Service {
    override suspend fun execute(
        request: ConfirmLogin2Service.Request
    ): Response<ConfirmLogin2Service.Data> = client.execute(
        path = ConfirmLogin2Service.URL,
        request = request,
        businessExceptions = emptyMap()
    )
}