package com.dashlane.nitro.api

import com.dashlane.nitro.api.endpoints.ConfirmLoginServiceImpl
import com.dashlane.nitro.api.endpoints.RequestLoginServiceImpl

internal class NitroApiImpl(
    private val client: NitroApiClient
) : NitroApi {
    override val endpoints = object : NitroApi.Endpoints {
        override val requestLoginService = RequestLoginServiceImpl(client)

        override val confirmLoginService = ConfirmLoginServiceImpl(client)
    }
}