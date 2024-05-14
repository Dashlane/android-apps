package com.dashlane.item.subview.provider.credential

import com.dashlane.preference.UserPreferencesManager
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class PasswordChangedInfoBoxDataProvider @Inject constructor(
    private val dataChangeHistoryQuery: DataChangeHistoryQuery,
    private val userPreferencesManager: UserPreferencesManager
) {
    fun isPasswordChangedInfoBoxNeeded(item: VaultItem<SyncObject.Authentifiant>): Boolean {
        val changeDate = getLastPasswordChanged(item)
        val lastPasswordChangedInfoBoxClosed = userPreferencesManager.getPasswordChangedInfoBoxClosedInstant(item.uid)
        return changeDate?.plus(Duration.ofDays(30))?.isAfter(Instant.now()) == true &&
            (lastPasswordChangedInfoBoxClosed == null || changeDate.isAfter(lastPasswordChangedInfoBoxClosed))
    }

    fun getLastPasswordChanged(item: VaultItem<SyncObject.Authentifiant>): Instant? =
        item.getPreviousPassword(item.syncObject.password?.toString(), dataChangeHistoryQuery)?.first

    fun setInfoBoxClosed(item: VaultItem<*>) {
        userPreferencesManager.setPasswordChangedInfoBoxClosedInstant(item.uid)
    }
}