package com.dashlane.ui.util

import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.createGeneratedPassword
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.utils.Country
import java.time.Instant

object PasswordGeneratorCreator {
    @JvmStatic
    fun provideGeneratedPassword(authDomain: String, password: String, authId: String) =
        createGeneratedPassword(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                uid = generateUniqueIdentifier(),
                anonymousUID = generateUniqueIdentifier(),
                id = 0,
                formatLang = Country.UnitedStates,
                syncState = SyncState.MODIFIED
            ),
            authDomain = authDomain,
            generatedDate = Instant.now().epochSecond.toString(),
            password = password,
            authId = authId,
            platform = SyncObject.Platform.SERVER_ANDROID
        )
}