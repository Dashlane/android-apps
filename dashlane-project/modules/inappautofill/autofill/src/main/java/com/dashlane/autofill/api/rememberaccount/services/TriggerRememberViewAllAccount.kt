package com.dashlane.autofill.api.rememberaccount.services

import com.dashlane.autofill.api.rememberaccount.model.FormSourcesDataProvider
import com.dashlane.autofill.api.viewallaccounts.services.ViewAllAccountSelectionNotifier
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject



class TriggerRememberViewAllAccount @Inject constructor(
    private val formSourceDataProvider: FormSourcesDataProvider,
    @MainCoroutineDispatcher private val mainCoroutineDispatcher: CoroutineDispatcher
) : ViewAllAccountSelectionNotifier {

    

    @OptIn(DelicateCoroutinesApi::class)
    override fun onAccountSelected(formSource: AutoFillFormSource, account: SummaryObject.Authentifiant) {
        GlobalScope.launch(mainCoroutineDispatcher) {
            this.runCatching {
                formSourceDataProvider.link(formSource, account.id)
            }.exceptionOrNull()
        }
    }
}