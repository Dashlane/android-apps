package com.dashlane.autofill.api

import com.dashlane.autofill.emptywebsitewarning.AutofillEmptyWebsiteWarningService
import com.dashlane.autofill.core.AutoFillDataBaseAccess
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class AutofillEmptyWebsiteWarningServiceImpl @Inject constructor(
    private val autoFillDataBaseAccess: AutoFillDataBaseAccess
) : AutofillEmptyWebsiteWarningService {
    override fun fetchCredentialFromId(uid: String): SummaryObject.Authentifiant? =
        autoFillDataBaseAccess.loadSummary(uid)

    override suspend fun updateCredentialWebsite(uid: String, newUrl: String): VaultItem<SyncObject.Authentifiant>? {
        return autoFillDataBaseAccess.updateAuthentifiantWebsite(uid, newUrl)
    }
}