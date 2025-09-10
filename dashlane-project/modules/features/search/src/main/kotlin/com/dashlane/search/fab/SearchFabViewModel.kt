package com.dashlane.search.fab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.design.component.BadgeIcon
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.limitations.PasswordLimiter
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.search.R
import com.dashlane.ui.common.compose.components.fab.FabItem
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchFabViewModel @Inject constructor(
    private val passwordLimiter: PasswordLimiter,
) : ViewModel() {
    private val stateFlow: MutableViewStateFlow<SearchFabState.View, SearchFabState.SideEffect> =
        MutableViewStateFlow(
            SearchFabState.View(fabContent = buildFabContent()),
        )
    val fabState: ViewStateFlow<SearchFabState.View, SearchFabState.SideEffect>
        get() = stateFlow

    fun onPrimaryFabClick() {
        stateFlow.update { state ->
            state.copy(isExpanded = !state.isExpanded)
        }
    }

    fun onActionClick(action: FabItem.Action) {
        val itemType = (action.actionKey as? SyncObjectType) ?: return

        viewModelScope.launch {
            close()
            stateFlow.send(SearchFabState.SideEffect.CreateItem(itemType = itemType))
        }
    }

    fun close() {
        stateFlow.update { state ->
            state.copy(isExpanded = false)
        }
    }

    private fun buildFabContent(): FabItem.Menu = FabItem.Menu(
        items = listOf(
            
            FabItem.Action(
                textRes = R.string.menu_v2_password_button,
                iconToken = IconTokens.itemLoginOutlined,
                badgeContent = if (passwordLimiter.isPasswordLimitReached()) {
                    FabItem.BadgeContent(
                        textRes = R.string.menu_v3_upgrade,
                        icon = BadgeIcon.Leading(IconTokens.premiumOutlined),
                        mood = Mood.Warning,
                        intensity = Intensity.Quiet,
                    )
                } else {
                    null
                },
                actionKey = SyncObjectType.AUTHENTIFIANT,
            ),
            
            FabItem.Action(
                textRes = R.string.menu_v2_secure_notes_button,
                iconToken = IconTokens.itemSecureNoteOutlined,
                actionKey = SyncObjectType.SECURE_NOTE,
            ),
            
            buildPaymentSection(),
            
            buildPersonalInfoSection(),
            
            buildIdCardSection(),
        ),
        textRes = R.string.vault_fab_add_item,
        iconToken = IconTokens.actionAddOutlined,
    )

    private fun buildPaymentSection() = FabItem.Menu(
        textRes = R.string.menu_v2_payments_button,
        iconToken = IconTokens.itemPaymentOutlined,
        items = listOf(
            FabItem.Action(
                iconToken = IconTokens.itemPaymentOutlined,
                textRes = R.string.datatype_paymentcreditcard,
                actionKey = SyncObjectType.PAYMENT_CREDIT_CARD,
            ),
            FabItem.Action(
                iconToken = IconTokens.itemBankAccountOutlined,
                textRes = R.string.datatype_bank_statement,
                actionKey = SyncObjectType.BANK_STATEMENT,
            ),
        ),
    )

    private fun buildIdCardSection() = FabItem.Menu(
        textRes = R.string.menu_v2_ids_button,
        iconToken = IconTokens.itemIdOutlined,
        items = listOf(
            
            FabItem.Action(
                iconToken = IconTokens.itemIdOutlined,
                textRes = R.string.datatype_id_card,
                actionKey = SyncObjectType.ID_CARD,
            ),
            
            FabItem.Action(
                iconToken = IconTokens.itemPassportOutlined,
                textRes = R.string.datatype_passport,
                actionKey = SyncObjectType.PASSPORT,
            ),
            
            FabItem.Action(
                iconToken = IconTokens.itemDriversLicenseOutlined,
                textRes = R.string.datatype_driver_licence,
                actionKey = SyncObjectType.DRIVER_LICENCE,
            ),
            
            FabItem.Action(
                iconToken = IconTokens.itemSocialSecurityOutlined,
                textRes = R.string.datatype_social_security_statement,
                actionKey = SyncObjectType.SOCIAL_SECURITY_STATEMENT,
            ),
            
            FabItem.Action(
                iconToken = IconTokens.itemTaxNumberOutlined,
                textRes = R.string.datatype_fiscal_statement,
                actionKey = SyncObjectType.FISCAL_STATEMENT,
            ),
        ),
    )

    private fun buildPersonalInfoSection() = FabItem.Menu(
        textRes = R.string.menu_v2_contact_button,
        iconToken = IconTokens.itemPersonalInfoOutlined,
        items = listOf(
            
            FabItem.Action(
                iconToken = IconTokens.itemPersonalInfoOutlined,
                textRes = R.string.datatype_identity,
                actionKey = SyncObjectType.IDENTITY,
            ),
            
            FabItem.Action(
                iconToken = IconTokens.itemEmailOutlined,
                textRes = R.string.datatype_email,
                actionKey = SyncObjectType.EMAIL,
            ),
            
            FabItem.Action(
                iconToken = IconTokens.itemPhoneMobileOutlined,
                textRes = R.string.datatype_phone,
                actionKey = SyncObjectType.PHONE,
            ),
            
            FabItem.Action(
                iconToken = IconTokens.homeOutlined,
                textRes = R.string.datatype_address,
                actionKey = SyncObjectType.ADDRESS,
            ),
            
            FabItem.Action(
                iconToken = IconTokens.itemCompanyOutlined,
                textRes = R.string.datatype_company,
                actionKey = SyncObjectType.COMPANY,
            ),
            
            FabItem.Action(
                iconToken = IconTokens.webOutlined,
                textRes = R.string.datatype_personal_website,
                actionKey = SyncObjectType.PERSONAL_WEBSITE,
            ),
        ),
    )
}

sealed class SearchFabState : State {
    data class View(
        val fabContent: FabItem.Menu,
        val isExpanded: Boolean = false,
    ) : SearchFabState(), State.View

    sealed class SideEffect : SearchFabState(), State.SideEffect {
        data class CreateItem(val itemType: SyncObjectType) : SideEffect()
    }
}