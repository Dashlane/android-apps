package com.dashlane.cryptography

open class CryptographyException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

open class CryptographyBase64Exception(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyException(message, cause)