package com.dashlane.autofill.api.emptywebsitewarning

import com.dashlane.vault.summary.SummaryObject



interface AutofillEmptyWebsiteWarningService {
    fun fetchCredentialFromId(uid: String): SummaryObject.Authentifiant?

    suspend fun updateCredentialWebsite(uid: String, newUrl: String): SummaryObject.Authentifiant?
}
