package com.dashlane.authentication

import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.generateFixedSalt
import com.dashlane.util.generateUniqueIdentifier
import java.time.Instant

class SettingsFactoryImpl(
    private val saltGenerator: SaltGenerator
) : SettingsFactory {
    override fun generateSettings(
        time: Instant,
        cryptographyMarker: CryptographyMarker
    ) = Settings(
        anonymousUserId = generateUniqueIdentifier(),
        cryptographyFixedSalt = saltGenerator.generateFixedSalt(cryptographyMarker),
        cryptographyMarker = cryptographyMarker,
        time = time,
        usageLogToken = generateUniqueIdentifier()
    )
}