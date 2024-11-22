package com.dashlane.nitro

import com.dashlane.server.api.NitroApi

interface Nitro {
    @Throws(NitroException::class)
    suspend fun authenticate(nitroUrl: String, behindProxy: Boolean): NitroApi
}