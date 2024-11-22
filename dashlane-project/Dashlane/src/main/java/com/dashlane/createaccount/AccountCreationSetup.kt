package com.dashlane.createaccount

import android.content.Context
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.createEmail
import com.dashlane.vault.model.getDefaultCountry
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

class AccountCreationSetup @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val dataSaver: DataSaver,
    private val preferencesManager: PreferencesManager,
    private val dataSync: DataSync,
    private val teamSpaceAccessor: OptionalProvider<TeamSpaceAccessor>
) {

    suspend fun setupCreatedAccount(
        username: String,
        userOrigin: String?,
    ) {
        preferencesManager[username].putString(ConstantsPrefs.USER_ORIGIN, userOrigin)
        insertVaultEmail(username)
        dataSync.sync(Trigger.ACCOUNT_CREATION)
    }

    private suspend fun insertVaultEmail(
        username: String
    ) {
        val timeStamp = Instant.now()
        val (emailType, teamId) = teamSpaceAccessor.get()?.enforcedSpace?.let { SyncObject.Email.Type.PRO to it.teamId }
            ?: (SyncObject.Email.Type.PERSO to null)
        val email = createEmail(
            dataIdentifier = CommonDataIdentifierAttrsImpl(
                formatLang = context.getDefaultCountry(),
                syncState = SyncState.MODIFIED,
                locallyViewedDate = Instant.EPOCH,
                creationDate = timeStamp,
                userModificationDate = timeStamp,
                teamSpaceId = teamId ?: TeamSpace.Personal.teamId
            ),
            type = emailType,
            emailName = context.getString(R.string.email),
            emailAddress = username
        )

        dataSaver.save(email)
    }
}
