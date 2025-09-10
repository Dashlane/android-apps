package com.dashlane.authentication

import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.data.login.Settings
import java.time.Instant

interface SettingsFactory {
    fun generateSettings(time: Instant, cryptographyMarker: CryptographyMarker): Settings
}