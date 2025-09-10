package com.dashlane.autofill.linkservices.rememberaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.linkservices.model.FormSourcesDataProvider
import com.dashlane.autofill.linkservices.rememberaccount.RememberAccountState.SideEffect
import com.dashlane.autofill.linkservices.rememberaccount.RememberAccountState.View
import com.dashlane.autofill.linkservices.rememberaccount.view.RememberAccountContentPresenter
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.autofill.pause.presenter.AutofillFormSourceProvider
import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.teamspaces.isSpaceItem
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForList
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RememberAccountViewModel @Inject constructor(
    private val repository: RememberAccountRepository,
    private val autoFillDatabaseAccess: AutofillAnalyzerDef.DatabaseAccess,
    private val logger: RememberAccountLogger,
    private val formSourceDataProvider: FormSourcesDataProvider,
    private val autofillFormSourceProvider: AutofillFormSourceProvider,
    private val presenter: RememberAccountContentPresenter,
) : ViewModel() {
    val state: ViewStateFlow<View, SideEffect>
        get() = _state
    private val _state: MutableViewStateFlow<View, SideEffect> = MutableViewStateFlow(View.Initial)

    fun onViewStarted(itemId: String, formSource: AutoFillFormSource) {
        viewModelScope.launch {
            autoFillDatabaseAccess.loadSummary<SummaryObject.Authentifiant>(itemId)
                ?.let { summaryItem ->
                    repository.updateData(formSource = formSource, summaryItem = summaryItem)
                    val itemName = summaryItem.titleForList ?: ""
                    val serviceName = autofillFormSourceProvider.getServiceName(formSource)
                    _state.emit(
                        View.Loaded(
                            formSource = formSource,
                            title = presenter.getTitle(formSource),
                            description = presenter.getDescription(
                                formSource = formSource,
                                itemName = itemName,
                                linkServiceName = serviceName,
                            ),
                            itemIcon = AutofillFormSourceIcon.WebsiteUrl(
                                summaryItem.urlForUI() ?: "",
                            ),
                            itemName = itemName,
                            itemDescription = summaryItem.loginForUi,
                            linkServiceIcon = autofillFormSourceProvider.getIcon(formSource),
                            linkServiceName = serviceName,
                            linkServiceBadge = presenter.getServiceBadge(formSource),
                            primaryCtaText = presenter.getPrimaryCtaText(formSource),
                        ),
                    )
                    logger.logShowLinkPage()
                }
        }
    }

    fun onAcceptServiceLink() {
        viewModelScope.launch {
            repository.getData()?.let { (formSource, loginItem) ->
                val isProfessionalSpace =
                    loginItem.isSpaceItem() && loginItem.spaceId.isNotSemanticallyNull()
                logger.logLinkServiceAccepted(
                    itemId = loginItem.id,
                    space = if (isProfessionalSpace) {
                        Space.PROFESSIONAL
                    } else {
                        Space.PERSONAL
                    },
                    itemUrl = loginItem.urlForGoToWebsite,
                    autoFillFormSource = formSource,
                )
                formSourceDataProvider.link(
                    formSource = formSource,
                    authentifiantId = loginItem.id,
                )
                _state.send(SideEffect.FinishWithResult(shouldAutofill = true))
            }
        }
    }

    fun onRefuseServiceLink() {
        viewModelScope.launch {
            logger.logLinkServiceRefused()
            _state.send(SideEffect.FinishWithResult(shouldAutofill = true))
        }
    }

    fun onCancel() {
        viewModelScope.launch {
            logger.logLinkServiceCancel()
            _state.send(SideEffect.FinishWithResult(shouldAutofill = false))
        }
    }
}