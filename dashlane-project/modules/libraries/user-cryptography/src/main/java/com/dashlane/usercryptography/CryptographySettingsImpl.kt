package com.dashlane.usercryptography

import com.dashlane.authentication.getCryptographyMarkerOrDefault
import com.dashlane.cryptography.CryptographyFixedSalt
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.CryptographySettings
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.toCryptographyMarkerOrNull
import com.dashlane.session.repository.SettingsManager
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.xml.domain.SyncObject

internal class CryptographySettingsImpl(
    private val settingsManager: SettingsManager,
    private val saltGenerator: SaltGenerator,
    private val teamSpaceAccessor: TeamSpaceAccessor?
) : CryptographySettings {

    override fun getOrCreateMarker(keyType: CryptographyKey.Type): CryptographyMarker {
        
        val teamMarker =
            teamSpaceAccessor?.cryptoForcedPayload?.toCryptographyMarkerOrNull()
        if (teamMarker != null && (teamMarker.keyType == keyType)) return teamMarker

        
        val settings = settingsManager.getSettings()
        return settings.getCryptographyMarkerOrDefault(keyType)
    }

    override fun getOrCreateFixedSalt(length: Int): CryptographyFixedSalt {
        val settings = settingsManager.getSettings()
        val fixedSalt = settings.cryptoFixedSalt
        return CryptographyFixedSalt(
            when {
                fixedSalt == null || fixedSalt.size < length ->
                    createFixedSaltValue(length, settings)
                fixedSalt.size > length ->
                    fixedSalt.copyOfRange(0, length)
                else ->
                    fixedSalt
            }
        )
    }

    private fun createFixedSaltValue(
        length: Int,
        settings: SyncObject.Settings
    ): ByteArray {
        val salt = saltGenerator.generateRandomSalt(length)
        updateSettings(settings) {
            cryptoFixedSalt = salt
        }
        return salt
    }

    private inline fun updateSettings(
        settings: SyncObject.Settings,
        builder: SyncObject.Settings.Builder.() -> Unit = {}
    ) {
        settingsManager.updateSettings(settings.copy(builder))
    }
}