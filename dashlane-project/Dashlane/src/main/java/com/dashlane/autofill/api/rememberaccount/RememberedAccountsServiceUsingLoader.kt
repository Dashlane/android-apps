package com.dashlane.autofill.api.rememberaccount

import com.dashlane.autofill.api.fillresponse.RememberedAccountsService
import com.dashlane.autofill.api.internal.FetchAccounts
import com.dashlane.autofill.api.rememberaccount.model.FormSourcesDataProvider
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject




class RememberedAccountsServiceUsingLoader @Inject constructor(
    private val formSourcesDataProvider: FormSourcesDataProvider,
    private val fetchAccounts: FetchAccounts,
    private val userFeaturesChecker: UserFeaturesChecker
) : RememberedAccountsService {

    

    override suspend fun fetchRememberedAccounts(formSource: AutoFillFormSource): List<SummaryObject.Authentifiant>? {
        return if (userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.LINKED_WEBSITES_IN_CONTEXT)) {
            
            return null
        } else {
            val rememberedAccountsIds =
                formSourcesDataProvider.getAllLinkedFormSourceAuthentifiantIds(formSource)
            fetchAccounts.fetchAccount(rememberedAccountsIds)
        }
    }

    override suspend fun isAccountRemembered(formSource: AutoFillFormSource, authentifiantId: String): Boolean {
        return formSourcesDataProvider.isLinked(formSource, authentifiantId)
    }
}
