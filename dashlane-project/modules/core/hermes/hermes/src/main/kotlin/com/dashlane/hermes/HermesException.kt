package com.dashlane.hermes

open class HermesException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)