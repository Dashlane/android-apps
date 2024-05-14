package com.dashlane.nitro

open class NitroException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

class NitroCryptographyException internal constructor(
    message: String? = null,
    cause: Throwable? = null
) : NitroException(message, cause)