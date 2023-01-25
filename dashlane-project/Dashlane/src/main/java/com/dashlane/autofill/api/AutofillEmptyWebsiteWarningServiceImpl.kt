package com.dashlane.autofill.api

import com.dashlane.autofill.api.emptywebsitewarning.AutofillEmptyWebsiteWarningService
import com.dashlane.autofill.core.AutoFillDataBaseAccess
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class AutofillEmptyWebsiteWarningServiceImpl @Inject constructor(
    private val autoFillDataBaseAccess: AutoFillDataBaseAccess
) : AutofillEmptyWebsiteWarningService {
    override fun fetchCredentialFromId(uid: String): SummaryObject.Authentifiant? =
        autoFillDataBaseAccess.loadSummaryAuthentifiant(uid)

    override suspend fun updateCredentialWebsite(uid: String, newUrl: String): SummaryObject.Authentifiant? {
        return autoFillDataBaseAccess.updateAuthentifiantWebsite(uid, newUrl)
    }
}