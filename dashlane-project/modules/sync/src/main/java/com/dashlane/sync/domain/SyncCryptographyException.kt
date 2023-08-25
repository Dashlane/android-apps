package com.dashlane.sync.domain

class SyncCryptographyException(
    message: String? = null,
    cause: Throwable? = null,
    val cipherPayload: String? = null
) : Exception(message, cause)