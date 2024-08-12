package com.dashlane.item.v3.util

import android.view.MenuItem
import com.dashlane.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.subview.action.ItemHistoryAction
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.action.NewShareMenuAction
import com.dashlane.item.subview.action.ShowAttachmentsMenuAction
import com.dashlane.item.subview.action.payment.CreditCardColorMenuAction
import com.dashlane.item.v3.data.CreditCardFormData
import com.dashlane.item.v3.data.ItemAction
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.viewmodels.State
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.attachmentsAllowed
import com.dashlane.vault.util.hasAttachments
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class MenuActionHelper @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
    private val restrictionNotificator: TeamSpaceRestrictionNotificator,
    private val itemEditRepository: ItemEditRepository,
    private val frozenStateManager: FrozenStateManager
) {

    @Suppress("LongMethod")
    fun getMenuActions(
        uiState: MutableStateFlow<State>,
        state: State,
        isEditMode: Boolean,
        saveAction: () -> Unit,
        switchModeAction: (State) -> State
    ): List<MenuAction> {
        val allMenus = mutableListOf<MenuAction>()
        val vaultItem = vaultDataQuery.queryLegacy(vaultFilter { specificUid(state.formData.id) })
        val summaryObject = vaultItem?.toSummary<SummaryObject>()
        val formData = state.formData
        
        when (formData) {
            is CreditCardFormData -> {
                val colorSelectAction: (SyncObject.PaymentCreditCard.Color) -> Unit = { color ->
                    uiState.update { s ->
                        if (s.formData !is CreditCardFormData) return@update s
                        s.copy(formData = s.formData.copy(color = color))
                    }
                }
                allMenus.add(
                    CreditCardColorMenuAction(
                        summaryObject as SummaryObject.PaymentCreditCard,
                        colorSelectAction
                    ) {
                        return@CreditCardColorMenuAction null
                    }
                )
            }
            else -> {
                
            }
        }

        
        if (summaryObject?.attachmentsAllowed(userFeaturesChecker) == true) {
            allMenus.add(ShowAttachmentsMenuAction(summaryObject, enabled = !frozenStateManager.isAccountFrozen))
        }

        
        if (formData !is SecureNoteFormData) {
            
            val canShare = !state.isNew && !isEditMode &&
                summaryObject != null &&
                sharingPolicyDataProvider.canShareItem(summaryObject) &&
                !summaryObject.hasAttachments() &&
                !frozenStateManager.isAccountFrozen
            if (isEditMode) {
                allMenus.add(
                    MenuAction(
                        text = R.string.dashlane_save,
                        icon = IconTokens.checkmarkOutlined.resource,
                        displayFlags = MenuItem.SHOW_AS_ACTION_ALWAYS
                    ) {
                        saveAction()
                    }
                )
            } else {
                
                summaryObject?.let {
                    allMenus.add(NewShareMenuAction(it, restrictionNotificator, enabled = canShare))
                }

                
                vaultItem?.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)?.takeIf {
                    itemEditRepository.hasPasswordHistory(it)
                }?.let {
                    allMenus.add(
                        ItemHistoryAction(enabled = state.formData.isEditable) {
                            uiState.update { it.copy(itemAction = ItemAction.OpenPasswordHistory) }
                        }
                    )
                }

                
                allMenus.add(
                    MenuAction(
                        text = R.string.edit,
                        icon = IconTokens.actionEditOutlined.resource,
                        displayFlags = MenuItem.SHOW_AS_ACTION_ALWAYS,
                    ) {
                        uiState.update { switchModeAction(it) }
                    }
                )
            }
        }
        return allMenus
    }
}