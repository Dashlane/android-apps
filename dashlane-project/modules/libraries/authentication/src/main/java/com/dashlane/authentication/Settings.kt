package com.dashlane.authentication

import com.dashlane.cryptography.CryptographyFixedSalt
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.xml.domain.SyncObject
import java.time.Instant

data class Settings(
    val anonymousUserId: String,
    val cryptographyFixedSalt: CryptographyFixedSalt?,
    val cryptographyMarker: CryptographyMarker,
    val time: Instant,
    val usageLogToken: String
)

internal fun Settings.toSyncObject() = SyncObject.Settings.create(
    anonymousUserId = anonymousUserId,
    cryptoUserPayload = cryptographyMarker.value,
    usagelogToken = usageLogToken
).copy {
    accountCreationDatetime = time.epochSecond
    cryptoFixedSalt = cryptographyFixedSalt?.data
}