package com.dashlane.item.v3.util

import android.view.MenuItem
import com.dashlane.R
import com.dashlane.design.iconography.IconTokens
import com.dashlane.featureflipping.FeatureFlip.ATTACHMENT_ALL_ITEMS
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.subview.action.ItemHistoryAction
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.action.NewShareMenuAction
import com.dashlane.item.subview.action.ShowAttachmentsMenuAction
import com.dashlane.item.subview.action.payment.CreditCardColorMenuAction
import com.dashlane.item.v3.data.CreditCardFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.viewmodels.ItemEditSideEffect
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.securefile.extensions.hasAttachments
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.utils.attachmentsAllowed
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MenuActionHelper @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
    private val restrictionNotificator: TeamSpaceRestrictionNotificator,
    private val itemEditRepository: ItemEditRepository,
    private val frozenStateManager: FrozenStateManager
) {

    @Suppress("LongMethod")
    fun <T : FormData> getMenuActions(
        uiState: MutableViewStateFlow<ItemEditState<T>, ItemEditSideEffect>,
        coroutineScope: CoroutineScope,
        state: ItemEditState<T>,
        isEditMode: Boolean,
        saveAction: () -> Unit,
        switchModeAction: (ItemEditState<T>) -> ItemEditState<T>
    ): List<MenuAction> {
        val allMenus = mutableListOf<MenuAction>()
        val vaultItem = state.itemId?.let { itemId ->
            vaultDataQuery.queryLegacy(vaultFilter { specificUid(itemId) })
        }
        val summaryObject = vaultItem?.toSummary<SummaryObject>()
        val formData = state.datas?.current?.formData
        
        when (formData) {
            is CreditCardFormData -> {
                val colorSelectAction: (SyncObject.PaymentCreditCard.Color) -> Unit = { color ->
                    uiState.update { s ->
                        s.datas ?: return@update s
                        if (s.datas.current.formData !is CreditCardFormData) return@update s

                        @Suppress("UNCHECKED_CAST")
                        s.copy(
                            datas = s.datas.copy(
                                current = s.datas.current.copy(
                                    formData = s.datas.current.formData.copy(
                                        color = color
                                    ) as T
                                ),
                                initial = s.datas.initial
                            )
                        )
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

        
        if (
            summaryObject?.attachmentsAllowed(
                attachmentAllItems = userFeaturesChecker.has(ATTACHMENT_ALL_ITEMS),
                isAccountFrozen = false,
                hasCollections = !state.datas?.current?.commonData?.collections.isNullOrEmpty()
            ) == true
        ) {
            allMenus.add(
                ShowAttachmentsMenuAction(enabled = !frozenStateManager.isAccountFrozen) {
                    coroutineScope.launch {
                        uiState.send(ItemEditSideEffect.ShowAttachments(summaryObject))
                    }
                }
            )
        }

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
            
            val canShare = !state.isNew && !isEditMode &&
                summaryObject != null &&
                sharingPolicyDataProvider.canShareItem(summaryObject) &&
                !summaryObject.hasAttachments() &&
                !frozenStateManager.isAccountFrozen &&
                
                summaryObject !is SummaryObject.Secret

            
            summaryObject?.let {
                allMenus.add(NewShareMenuAction(it, restrictionNotificator, enabled = canShare))
            }

            
            vaultItem?.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)?.takeIf {
                itemEditRepository.hasPasswordHistory(it)
            }?.let {
                allMenus.add(
                    ItemHistoryAction(enabled = state.datas?.current?.commonData?.isEditable ?: false) {
                        coroutineScope.launch {
                            uiState.value.datas?.current?.let {
                                uiState.send(ItemEditSideEffect.OpenPasswordHistory(it.commonData.id))
                            }
                        }
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
        return allMenus
    }
}