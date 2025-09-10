package com.dashlane.autofill.linkservices.services

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.linkservices.model.FormSourcesDataProvider
import com.dashlane.autofill.viewallaccounts.services.ViewAllAccountSelectionNotifier
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class TriggerRememberViewAllAccount @Inject constructor(
    private val formSourceDataProvider: FormSourcesDataProvider,
    @MainCoroutineDispatcher private val mainCoroutineDispatcher: CoroutineDispatcher,
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
) : ViewAllAccountSelectionNotifier {
    override fun onAccountSelected(formSource: AutoFillFormSource, account: SummaryObject.Authentifiant) {
        applicationCoroutineScope.launch(mainCoroutineDispatcher) {
            this.runCatching {
                formSourceDataProvider.link(formSource, account.id)
            }.exceptionOrNull()
        }
    }
}