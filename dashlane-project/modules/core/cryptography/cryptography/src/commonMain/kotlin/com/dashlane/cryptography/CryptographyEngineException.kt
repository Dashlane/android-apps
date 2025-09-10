package com.dashlane.cryptography

open class CryptographyEngineException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyException(message, cause) {
    var marker: CryptographyMarker? = null
        internal set
}

internal inline fun <T> tryCryptographyEngine(marker: CryptographyMarker, block: () -> T): T =
    try {
        block()
    } catch (e: CryptographyEngineException) {
        e.marker = marker
        throw e
    }

open class CryptographyCipherException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEngineException(message, cause)

open class CryptographyCompressionException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEngineException(message, cause)

open class CryptographyMarkerException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEngineException(message, cause)

open class CryptographyHeaderException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEngineException(message, cause)

open class CryptographyCompressionHeaderException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEngineException(message, cause)

open class CryptographyChecksumException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEngineException(message, cause)

open class CryptographyUnsupportedException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEngineException(message, cause)

open class CryptographyIllegalKeyException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEngineException(message, cause)

open class CryptographyEncryptionSettingsArgumentException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEngineException(message, cause)

open class CryptographyInvalidSaltException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyEncryptionSettingsArgumentException(message, cause)

open class CryptographyInvalidSaltLengthException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyInvalidSaltException(message, cause)

open class CryptographyFixedSaltRequiredException(
    message: String? = null,
    cause: Throwable? = null,
) : CryptographyInvalidSaltException(message, cause)