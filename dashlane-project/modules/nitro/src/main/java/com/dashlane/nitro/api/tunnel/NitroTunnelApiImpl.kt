package com.dashlane.nitro.api.tunnel

import com.dashlane.nitro.api.tunnel.endpoints.ClientHelloServiceImpl
import com.dashlane.nitro.api.tunnel.endpoints.TerminateHelloServiceImpl
import com.dashlane.server.api.DashlaneApiClient

internal class NitroTunnelApiImpl(
    private val client: DashlaneApiClient
) : NitroTunnelApi {
    override val endpoints = object : NitroTunnelApi.Endpoints {
        override val clientHelloService = ClientHelloServiceImpl(client)
        override val terminateHelloService = TerminateHelloServiceImpl(client)
    }
}