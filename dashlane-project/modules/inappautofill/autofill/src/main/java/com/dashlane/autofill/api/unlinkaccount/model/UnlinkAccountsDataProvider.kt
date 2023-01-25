package com.dashlane.autofill.api.unlinkaccount.model

import com.dashlane.autofill.api.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.api.internal.FetchAccounts
import com.dashlane.autofill.api.rememberaccount.model.FormSourcesDataProvider
import com.dashlane.autofill.api.unlinkaccount.UnlinkAccountsContract
import com.dashlane.autofill.api.unlinkaccount.dagger.Data
import com.dashlane.autofill.api.unlinkaccount.dagger.ViewModel
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext



class UnlinkAccountsDataProvider @Inject constructor(
    private val autoFillFormSource: AutoFillFormSource,
    private val formSourcesDataProvider: FormSourcesDataProvider,
    private val fetchAccounts: FetchAccounts,
    autofillFormSourcesStrings: AutofillFormSourcesStrings,
    @ViewModel
    private val viewModelScope: CoroutineScope,
    @Data
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : UnlinkAccountsContract.DataProvider {
    private val mutex = Mutex()
    private var lastState: LinkedAccountsModel? = null
    private var responses: UnlinkAccountsContract.DataProvider.Responses? = null
    private val autofillFormSourceTitle = autofillFormSourcesStrings.getAutoFillFormSourceString(autoFillFormSource)

    override fun bindResponses(responses: UnlinkAccountsContract.DataProvider.Responses) {
        this.responses = responses
    }

    override fun currentState(): LinkedAccountsModel? {
        return lastState
    }

    override fun loadLinkedAccounts() {
        viewModelScope.launch(coroutineContext) {
            mutex.withLock {
                var loadFormSourcesState =
                    lastState?.copy(processing = true) ?: LinkedAccountsModel(
                        processing = true,
                        autoFillFormSourceTitle = autofillFormSourceTitle
                    )
                try {
                    lastState = loadFormSourcesState
                    val allFormSource = getAllFormSource()
                    loadFormSourcesState = loadFormSourcesState.copy(
                        processing = false,
                        linkedAccounts = allFormSource
                    )
                    lastState = loadFormSourcesState
                    responses?.updateLinkedAccounts(loadFormSourcesState)
                } catch (e: Exception) {
                    loadFormSourcesState = loadFormSourcesState.copy(processing = false)
                    lastState = loadFormSourcesState
                    responses?.errorOnLoadLinkedAccounts()
                }
            }
        }
    }

    override fun unlinkAccount(index: Int) {
        if (!mutex.isLocked) {
            viewModelScope.launch(coroutineContext) {
                mutex.withLock {
                    try {
                        var workingState = lastState?.copy(processing = true)
                            ?: throw IllegalStateException("unlink over invalid state")

                        val unlinkAccount = workingState.linkedAccounts[index]

                        formSourcesDataProvider.unlink(autoFillFormSource, unlinkAccount.id)
                        workingState = LinkedAccountsModel(false, getAllFormSource(), autofillFormSourceTitle)
                        lastState = workingState
                        responses?.unlinkedAccount(unlinkAccount, workingState)
                    } catch (e: Exception) {
                        println(e)
                        responses?.errorOnUnlinkAccount()
                    }
                }
            }
        } else {
            responses?.errorOnUnlinkAccount()
        }
    }

    private suspend fun getAllFormSource(): List<SummaryObject.Authentifiant> {
        val linkedAccountsIds =
            formSourcesDataProvider.getAllLinkedFormSourceAuthentifiantIds(autoFillFormSource)
        return fetchAccounts.fetchAccount(linkedAccountsIds) ?: emptyList()
    }
}
