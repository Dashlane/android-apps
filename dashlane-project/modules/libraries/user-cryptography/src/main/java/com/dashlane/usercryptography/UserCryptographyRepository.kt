package com.dashlane.usercryptography

import com.dashlane.authentication.cryptographyMarker
import com.dashlane.crypto.keys.VaultKey
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyMarker
import com.dashlane.cryptography.CryptographySettings
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.toCryptographyMarkerOrNull
import com.dashlane.session.Session
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
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
