package com.dashlane.nitro

import com.dashlane.nitro.api.NitroApi

interface Nitro {
    @Throws(NitroException::class)
    suspend fun authenticate(nitroUrl: String): NitroApi
}