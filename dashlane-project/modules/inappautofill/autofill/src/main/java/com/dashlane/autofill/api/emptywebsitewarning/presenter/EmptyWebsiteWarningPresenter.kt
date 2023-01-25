package com.dashlane.autofill.api.emptywebsitewarning.presenter

import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningContract
import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningLogger
import com.dashlane.vault.model.getLoginForUi
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class EmptyWebsiteWarningPresenter @Inject constructor(
    private val dataProvider: EmptyWebsiteWarningContract.DataProvider,
    private val logger: EmptyWebsiteWarningLogger
) : EmptyWebsiteWarningContract.Presenter {

    override suspend fun getAccountWrapper(
        uid: String,
        currentUrl: String
    ): EmptyWebsiteWarningContract.AccountWrapper? {
        val authentifiant = fetchAccount(uid) ?: return null

        return EmptyWebsiteWarningContract.AccountWrapper(
            uid,
            authentifiant.title ?: "",
            authentifiant.getLoginForUi(false) ?: "",
            currentUrl
        )
    }

    override suspend fun fetchAccount(uid: String): SummaryObject.Authentifiant? = dataProvider.fetchAccount(uid)

    override suspend fun updateAccountWithNewUrl(uid: String, website: String): SummaryObject.Authentifiant? {
        val result = dataProvider.updateAccountWithNewUrl(uid, website)
        logger.logUpdateAccount(website)
        return result
    }

    override fun onCancel(website: String) = logger.logCancel(website)

    override fun onDisplay(website: String) = logger.logDisplay(website)
}