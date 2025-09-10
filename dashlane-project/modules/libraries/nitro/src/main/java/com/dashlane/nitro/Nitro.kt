package com.dashlane.nitro

import com.dashlane.server.api.NitroApi
import com.dashlane.server.api.Response
import com.dashlane.server.api.exceptions.DashlaneApiIoException

interface Nitro {
    @Throws(NitroException::class)
    suspend fun authenticate()

    @Throws(NitroException::class)
    suspend fun authenticate(newNitroHost: String)

    @Throws(NitroInitializationException::class, NitroException::class)
    suspend fun <T> execute(block: suspend NitroApi.() -> Response<T>): Response<T>

    class NitroInitializationException : DashlaneApiIoException(message = "Nitro API not initialized")
}