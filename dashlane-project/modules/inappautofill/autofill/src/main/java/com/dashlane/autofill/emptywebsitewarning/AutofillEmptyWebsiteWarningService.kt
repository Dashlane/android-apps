package com.dashlane.autofill.emptywebsitewarning

import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

interface AutofillEmptyWebsiteWarningService {
    fun fetchCredentialFromId(uid: String): SummaryObject.Authentifiant?

    suspend fun updateCredentialWebsite(uid: String, newUrl: String): VaultItem<SyncObject.Authentifiant>?
}
