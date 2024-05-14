package com.dashlane.nitro.api.tunnel

import com.dashlane.nitro.api.tunnel.endpoints.ClientHelloService
import com.dashlane.nitro.api.tunnel.endpoints.TerminateHelloService

internal interface NitroTunnelApi {
    val endpoints: Endpoints

    interface Endpoints {
        val clientHelloService: ClientHelloService
        val terminateHelloService: TerminateHelloService
    }
}