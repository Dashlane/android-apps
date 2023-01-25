package com.dashlane.nitro.api.endpoints

import com.dashlane.nitro.api.NitroApiClient
import com.dashlane.nitro.api.execute
import com.dashlane.server.api.Response

internal class ConfirmLoginServiceImpl(
    private val client: NitroApiClient
) : ConfirmLoginService {
    override suspend fun execute(
        request: ConfirmLoginService.Request
    ): Response<ConfirmLoginService.Data> = client.execute(
        path = "/api/authentication/ConfirmLogin",
        request = request,
        businessExceptions = emptyMap()
    )
}