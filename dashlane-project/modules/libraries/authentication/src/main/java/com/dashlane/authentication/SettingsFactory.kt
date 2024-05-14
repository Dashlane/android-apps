package com.dashlane.authentication

import com.dashlane.cryptography.CryptographyMarker
import java.time.Instant

interface SettingsFactory {
    fun generateSettings(time: Instant, cryptographyMarker: CryptographyMarker): Settings
}