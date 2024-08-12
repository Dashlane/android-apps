package com.dashlane.autofill.emptywebsitewarning.presenter

import com.dashlane.autofill.emptywebsitewarning.EmptyWebsiteWarningContract
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class EmptyWebsiteWarningPresenter @Inject constructor(
    private val dataProvider: EmptyWebsiteWarningContract.DataProvider
) : EmptyWebsiteWarningContract.Presenter {

    override suspend fun getAccountWrapper(
        uid: String,
        currentUrl: String
    ): EmptyWebsiteWarningContract.AccountWrapper? {
        val authentifiant = fetchAccount(uid) ?: return null

        return EmptyWebsiteWarningContract.AccountWrapper(
            uid,
            authentifiant.title ?: "",
            authentifiant.loginForUi ?: "",
            currentUrl
        )
    }

    override suspend fun fetchAccount(uid: String): SummaryObject.Authentifiant? = dataProvider.fetchAccount(uid)

    override suspend fun updateAccountWithNewUrl(
        uid: String,
        website: String
    ): VaultItem<SyncObject.Authentifiant>? {
        return dataProvider.updateAccountWithNewUrl(uid, website)
    }
}