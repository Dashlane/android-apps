package com.dashlane.session.repository

import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyFixedSalt
import com.dashlane.cryptography.CryptographyKey
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.CryptographySettings
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.toCryptographyMarkerOrNull
import com.dashlane.session.Session
import com.dashlane.session.UserDataRepository
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.settings.SettingsManager
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.xml.domain.SyncObject
import dagger.Lazy
import javax.inject.Inject

interface UserCryptographyRepository {

    fun getCryptographyMarker(session: Session): CryptographyMarker?

    fun getCryptographyEngineFactory(session: Session): CryptographyEngineFactory

    fun getCryptographySettings(session: Session): CryptographySettings
}

class UserCryptographyRepositoryImpl @Inject constructor(
    private val cryptography: Cryptography,
    private val saltGenerator: SaltGenerator,
    private val userDataRepositoryLazy: Lazy<UserDataRepository>,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>
) : UserCryptographyRepository {

    private val userDataRepository
        get() = userDataRepositoryLazy.get()

    private val teamSpaceAccessor
        get() = teamSpaceAccessorProvider.get()

    override fun getCryptographyMarker(session: Session): CryptographyMarker? {
        val keyType = session.appKeyType

        
        if (teamSpaceAccessor?.isSsoUser != true) {
            val teamMarker = teamSpaceAccessor?.cryptoForcedPayload?.toCryptographyMarkerOrNull()
            if (teamMarker != null && teamMarker.keyType == keyType) return teamMarker
        }

        
        val settings = getSettingsManager(session).getSettings()
        return settings.cryptographyMarker?.takeIf { it.keyType == keyType } 
    }

    override fun getCryptographyEngineFactory(session: Session): CryptographyEngineFactory =
        CryptographyEngineFactory(
            cryptography,
            session.vaultKey.use(VaultKey::cryptographyKey),
            getCryptographySettings(session)
        )

    override fun getCryptographySettings(session: Session): CryptographySettings =
        CryptographySettingsImpl(getSettingsManager(session), saltGenerator, teamSpaceAccessor)

    private fun getSettingsManager(session: Session) =
        userDataRepository.getSettingsManager(session)
}

private class CryptographySettingsImpl(
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

val SyncObject.Settings.cryptographyMarker: CryptographyMarker?
    get() = cryptoUserPayload?.toCryptographyMarkerOrNull()

fun SyncObject.Settings.getCryptographyMarkerOrDefault(keyType: CryptographyKey.Type) =
    cryptographyMarker?.takeIf { it.keyType == keyType } ?: getDefaultMarker(keyType)

private fun getDefaultMarker(keyType: CryptographyKey.Type) = when (keyType) {
    CryptographyKey.Type.PASSWORD ->
        CryptographyMarker.Flexible.Defaults.argon2d
    CryptographyKey.Type.RAW_32 ->
        CryptographyMarker.Flexible.Defaults.noDerivation
    CryptographyKey.Type.RAW_64 ->
        CryptographyMarker.Flexible.Defaults.noDerivation64
}