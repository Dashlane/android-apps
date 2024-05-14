package com.dashlane.nitro.api

import com.dashlane.nitro.api.endpoints.ConfirmLogin2ServiceImpl
import com.dashlane.nitro.api.endpoints.RequestLogin2ServiceImpl

internal class NitroApiImpl(
    private val client: NitroApiClient
) : NitroApi {
    override val endpoints = object : NitroApi.Endpoints {
        override val requestLogin2Service = RequestLogin2ServiceImpl(client)

        override val confirmLogin2Service = ConfirmLogin2ServiceImpl(client)
    }
}