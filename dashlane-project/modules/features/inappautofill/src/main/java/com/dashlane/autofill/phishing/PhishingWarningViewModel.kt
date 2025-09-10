package com.dashlane.autofill.phishing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.linkservices.model.FormSourcesDataProvider
import com.dashlane.autofill.phishing.PhishingWarningState.SideEffect
import com.dashlane.autofill.phishing.PhishingWarningState.ViewState
import com.dashlane.hermes.generated.definitions.PhishingRisk
import com.dashlane.inappautofill.AutofillPhishingLogger
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.util.toRootUrlDomain
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhishingWarningViewModel @Inject constructor(
    private val phishingWarningRepository: PhishingWarningRepository,
    private val formSourceDataProvider: FormSourcesDataProvider,
    private val autoFillDatabaseAccess: AutofillAnalyzerDef.DatabaseAccess,
    private val logger: AutofillPhishingLogger,
) : ViewModel() {
    private val viewStateFlow = MutableViewStateFlow<ViewState, SideEffect>(ViewState.Initial)
    val state: ViewStateFlow<ViewState, SideEffect> = viewStateFlow

    fun onViewStarted(itemId: String, webFormSource: WebDomainFormSource) {
        viewModelScope.launch {
            autoFillDatabaseAccess.loadSummary<SummaryObject.Authentifiant>(itemId)
                ?.let { summaryItem ->
                    phishingWarningRepository.updateData(
                        webFormSource = webFormSource,
                        summaryItem = summaryItem,
                    )
                    viewStateFlow.emit(
                        ViewState.Loaded(
                            itemWebsite = formatWebsiteForUi(summaryItem.urlDomain),
                            suspiciousWebsite = formatWebsiteForUi(webFormSource.webDomain),
                        ),
                    )
                }
        }
    }

    fun dismiss() {
        viewModelScope.launch {
            phishingWarningRepository.getData()?.let { (webFormSource, _) ->
                logger.onDismissAutoFill(autoFillFormSource = webFormSource, trust = false)
                viewStateFlow.send(SideEffect.OnResult(shouldAutofill = false))
            }
        }
    }

    fun trustAndAllowAutofill() {
        viewModelScope.launch {
            phishingWarningRepository.getData()?.let { (webFormSource, summaryItem) ->
                logger.onAcceptAutoFillRisk(
                    autoFillFormSource = webFormSource,
                    phishingRisk = PhishingRisk.NONE,
                )
                saveRememberWebsite(
                    summaryItem = summaryItem,
                    webFormSource = webFormSource,
                )
            }
        }
    }

    private suspend fun saveRememberWebsite(
        summaryItem: SummaryObject.Authentifiant,
        webFormSource: WebDomainFormSource,
    ) {
        
        formSourceDataProvider.link(formSource = webFormSource, authentifiantId = summaryItem.id)

        viewStateFlow.send(SideEffect.OnResult(shouldAutofill = true))
    }

    private fun formatWebsiteForUi(url: String?): String {
        return url?.toRootUrlDomain()?.toString() ?: ""
    }
}