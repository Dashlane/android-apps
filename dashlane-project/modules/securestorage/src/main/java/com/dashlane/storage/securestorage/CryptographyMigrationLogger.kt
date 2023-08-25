package com.dashlane.storage.securestorage

import com.dashlane.cryptography.CryptographyMarker

interface CryptographyMigrationLogger {
    fun logChangeDetected(currentMarker: CryptographyMarker, expectedMarker: CryptographyMarker)

    fun logStart(currentMarker: CryptographyMarker, expectedMarker: CryptographyMarker)

    fun logSuccess(currentMarker: CryptographyMarker, expectedMarker: CryptographyMarker)

    fun logErrorDecrypt(expectedPayload: String)

    fun logErrorEncrypt(expectedPayload: String)

    fun logErrorPayloadNotMatch(expectedPayload: String)

    fun logErrorUnknown(expectedPayload: String)
}