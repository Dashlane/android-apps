package com.dashlane.autofill.rememberaccount.services

import com.dashlane.autofill.rememberaccount.model.FormSourcesDataProvider
import com.dashlane.autofill.viewallaccounts.services.ViewAllAccountSelectionNotifier
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class TriggerRememberViewAllAccount @Inject constructor(
    private val formSourceDataProvider: FormSourcesDataProvider,
    @MainCoroutineDispatcher private val mainCoroutineDispatcher: CoroutineDispatcher,
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope
) : ViewAllAccountSelectionNotifier {

    override fun onAccountSelected(formSource: AutoFillFormSource, account: SummaryObject.Authentifiant) {
        applicationCoroutineScope.launch(mainCoroutineDispatcher) {
            this.runCatching {
                formSourceDataProvider.link(formSource, account.id)
            }.exceptionOrNull()
        }
    }
}
