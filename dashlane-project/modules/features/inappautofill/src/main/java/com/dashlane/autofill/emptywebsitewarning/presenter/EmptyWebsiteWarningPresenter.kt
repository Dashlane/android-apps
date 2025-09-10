package com.dashlane.autofill.emptywebsitewarning.presenter

import com.dashlane.autofill.emptywebsitewarning.AutofillEmptyWebsiteWarningService
import com.dashlane.autofill.emptywebsitewarning.model.AccountWrapper
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class EmptyWebsiteWarningPresenter @Inject constructor(
    private val service: AutofillEmptyWebsiteWarningService,
) {
    fun getAccountWrapper(
        uid: String,
        currentUrl: String,
    ): AccountWrapper? {
        val authentifiant = fetchAccount(uid) ?: return null

        return AccountWrapper(
            uid,
            authentifiant.title ?: "",
            authentifiant.loginForUi ?: "",
            currentUrl,
        )
    }

    private fun fetchAccount(uid: String): SummaryObject.Authentifiant? = service.fetchCredentialFromId(uid)

    suspend fun updateAccountWithNewUrl(
        uid: String,
        website: String,
    ): VaultItem<SyncObject.Authentifiant>? {
        return service.updateCredentialWebsite(uid, website)
    }
}