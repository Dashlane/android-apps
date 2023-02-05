package com.dashlane.ui.activities.fragments.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.ItemState
import com.dashlane.ui.menu.MenuDef
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val provider: ChecklistDataProvider,
    private val checklistHelper: ChecklistHelper,
    private val navigator: Navigator,
    private val menuPresenter: MenuDef.IPresenter
) : ViewModel(), ChecklistViewModelContract {

    override val checkListDataFlow =
        flow {
            val checklistData = buildChecklistData()
            emit(checklistData)
            provider.setChecklistSeen()
        }.shareIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    private suspend fun buildChecklistData(): ChecklistData {
        val addCredential = if (!provider.hasDarkWebMonitoring()) getAddCredentialChecklistItem() else null
        val checkDarkWebAlerts = if (provider.hasDarkWebMonitoring()) getCheckDarkWebAlertsChecklistItem() else null
        val activatedAutofill = getActivatedAutofillChecklistItem()
        val m2d = getM2dChecklistItem()
        val isDismissable =
            addCredential?.state !== ItemState.TO_COMPLETE && checkDarkWebAlerts?.state !== ItemState.TO_COMPLETE &&
                    activatedAutofill.state !== ItemState.TO_COMPLETE && m2d.state !== ItemState.TO_COMPLETE ||
                    checklistHelper.isAccountOlderThan7Days()
        return ChecklistData(
            addCredential,
            checkDarkWebAlerts,
            activatedAutofill,
            m2d,
            isDismissable,
            provider.hasSeenChecklist()
        )
    }

    private suspend fun getCheckDarkWebAlertsChecklistItem(): ChecklistItem {
        val state = getCheckListStepState(
            provider.hasCompletedAndAcknowledgedDarkWebAlerts(),
            provider.hasReadAllDarkWebAlerts()
        )
        val item = ChecklistItem(ChecklistData.ItemType.DARK_WEB_MONITORING, state, null)
        item.onChecklistItemAcknowledge = { this.onCheckDarkWebCompletionAcknowledged() }
        return item
    }

    private fun getAddCredentialChecklistItem(): ChecklistItem {
        val addCredentialStatus = getCheckListStepState(
            provider.hasCompletedAndAcknowledgedAddCredential(),
            provider.hasAddedAccounts()
        )
        val item = ChecklistItem(
            ChecklistData.ItemType.ADD_CREDENTIAL,
            addCredentialStatus,
            provider.getGuidedOnBoardingAction()
        )
        item.onChecklistItemAcknowledge = { this.onAddCredentialCompletionAcknowledged() }
        return item
    }

    private fun getActivatedAutofillChecklistItem(): ChecklistItem {
        val state = getCheckListStepState(
            provider.hasCompletedAndAcknowledgedAutofillActivation(),
            provider.hasActivatedAutofill()
        )
        val item = ChecklistItem(ChecklistData.ItemType.AUTOFILL, state, null)
        item.onChecklistItemAcknowledge = { this.onAutofillActivationCompletionAcknowledged() }
        return item
    }

    private fun getM2dChecklistItem(): ChecklistItem {
        val state = getCheckListStepState(
            provider.hasCompletedAndAcknowledgedM2D(),
            provider.hasFinishedM2D()
        )
        val item = ChecklistItem(ChecklistData.ItemType.M2D, state, null)
        item.onChecklistItemAcknowledge = { this.onM2DCompletionAcknowledged() }
        return item
    }

    private fun getCheckListStepState(isAcknowledged: Boolean, isCompleted: Boolean): ItemState =
        when {
            isAcknowledged -> ItemState.COMPLETED_AND_ACKNOWLEDGED
            isCompleted -> ItemState.COMPLETED
            else -> ItemState.TO_COMPLETE
        }

    override fun onDismissChecklistClicked() {
        
        provider.setChecklistDismissed()
        navigator.goToHome()
        
        menuPresenter.refreshMenuList()
    }

    private fun onAddCredentialCompletionAcknowledged() {
        provider.saveAddCredentialCompletedAndAcknowledged()
    }

    private fun onAutofillActivationCompletionAcknowledged() {
        provider.saveAutofillActivationCompletedAndAcknowledgedAutofill()
    }

    private fun onM2DCompletionAcknowledged() {
        provider.saveM2DCompletedAndAcknowledged()
    }

    private fun onCheckDarkWebCompletionAcknowledged() {
        provider.saveDarkWebMonitoringCompletedAndAcknowledged()
    }
}