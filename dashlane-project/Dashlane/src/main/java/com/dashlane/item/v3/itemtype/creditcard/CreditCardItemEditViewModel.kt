package com.dashlane.item.v3.itemtype.creditcard

import android.nfc.Tag
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.dashlane.events.AppEvents
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.nfc.NfcCreditCardReader
import com.dashlane.item.v3.itemtype.creditcard.builder.CreditCardFormBuilder
import com.dashlane.item.v3.itemtype.creditcard.builder.CreditCardHiddenFieldsBuilder
import com.dashlane.item.v3.itemtype.creditcard.builder.CreditCardTypeFormatBuilder
import com.dashlane.item.v3.itemtype.creditcard.loader.CreditCardSensitiveFieldLoader
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.util.MenuActionHelper
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.ItemEditSideEffect
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.item.v3.viewmodels.ItemEditViewModel
import com.dashlane.navigation.Navigator
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.vault.model.createPaymentCreditCard
import com.dashlane.vault.model.getColorResource
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@Suppress("LargeClass")
@HiltViewModel
class CreditCardItemEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    dataSync: DataSync,
    genericDataQuery: GenericDataQuery,
    appEvents: AppEvents,
    menuActionHelper: MenuActionHelper,
    formBuilder: CreditCardFormBuilder,
    vaultItemCopy: VaultItemCopyService,
    frozenStateManager: FrozenStateManager,
    itemEditRepository: ItemEditRepository,
    sharingPolicyDataProvider: SharingPolicyDataProvider,
    navigator: Navigator,
    vaultDataQuery: VaultDataQuery,
    private val creditCardSensitiveFieldLoader: CreditCardSensitiveFieldLoader,
    private val creditCardTypeFormatBuilder: CreditCardTypeFormatBuilder,
    private val creditCardHiddenFieldsBuilder: CreditCardHiddenFieldsBuilder,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    private val nfcCreditCardReader: NfcCreditCardReader,
    private val vibrator: Vibrator,
) : ItemEditViewModel<CreditCardFormData>(
        savedStateHandle = savedStateHandle,
        dataSync = dataSync,
        genericDataQuery = genericDataQuery,
        vaultDataQuery = vaultDataQuery,
        vaultItemCopy = vaultItemCopy,
        itemEditRepository = itemEditRepository,
        appEvents = appEvents,
        menuActionHelper = menuActionHelper,
        formDataBuilder = formBuilder,
        asyncDataLoader = null,
        frozenStateManager = frozenStateManager,
        sharingPolicyDataProvider = sharingPolicyDataProvider,
        navigator = navigator,
    ) {
    override fun createNewItem() = createPaymentCreditCard()

    @Suppress(
        
        "kotlin:S6313",
    )
    override suspend fun goToEditMode(state: ItemEditState<CreditCardFormData>): ItemEditState<CreditCardFormData> {
        return super.goToEditMode(state)
            .copy(hiddenFields = emptySet())
            .loadSensitiveField(SensitiveField.CREDIT_CARD_NUMBER)
            .loadSensitiveField(SensitiveField.CREDIT_CARD_SECURITY_CODE)
            .loadSensitiveField(SensitiveField.CREDIT_CARD_NOTE)
    }

    override fun goToViewMode(state: ItemEditState<CreditCardFormData>): ItemEditState<CreditCardFormData> {
        return super.goToViewMode(state)
            .copy(
                hiddenFields = state.datas?.current
                    ?.let { creditCardHiddenFieldsBuilder(it.formData, it.commonData) }
                    ?: state.hiddenFields,
            )
    }

    @Suppress(
        
        "kotlin:S6313",
    )
    override suspend fun ItemEditState<CreditCardFormData>.loadSensitiveField(field: SensitiveField): ItemEditState<CreditCardFormData> {
        return when (field) {
            SensitiveField.CREDIT_CARD_NUMBER -> {
                val cardNumber = creditCardSensitiveFieldLoader.loadCardNumber(this)
                copyCurrentFormData {
                    it.copy(cardNumber = cardNumber)
                }.copyInitialFormData { initial, _ ->
                    if (initial.cardNumber == null) {
                        initial.copy(cardNumber = cardNumber)
                    } else {
                        initial
                    }
                }
            }

            SensitiveField.CREDIT_CARD_SECURITY_CODE -> {
                val securityCode = creditCardSensitiveFieldLoader.loadSecurityCode(this)
                copyCurrentFormData {
                    it.copy(securityCode = securityCode)
                }.copyInitialFormData { initial, _ ->
                    if (initial.securityCode == null) {
                        initial.copy(securityCode = securityCode)
                    } else {
                        initial
                    }
                }
            }

            SensitiveField.CREDIT_CARD_NOTE -> {
                val note = creditCardSensitiveFieldLoader.loadNote(this)
                copyCurrentFormData {
                    it.copy(note = note)
                }.copyInitialFormData { initial, _ ->
                    if (initial.note == null) {
                        initial.copy(note = note)
                    } else {
                        initial
                    }
                }
            }

            else -> this
        }
    }

    override fun hasError(current: Data<CreditCardFormData>): Boolean =
        current.formData.isCardNumberEmpty

    fun handleAction(action: CreditCardItemEditAction) {
        when (action) {
            is CreditCardItemEditAction.CardHolderChanged -> onCardHolderChanged(action.value)
            is CreditCardItemEditAction.CardNumberChanged -> onCardNumberChanged(action.value)
            is CreditCardItemEditAction.SecurityCodeChanged -> onSecurityCodeChanged(action.value)
            is CreditCardItemEditAction.NoteChanged -> onNoteChanged(action.value)
            is CreditCardItemEditAction.ExpirationDateChanged -> onExpirationDateChanged(action.value)
            is CreditCardItemEditAction.ItemNameChanged -> itemNameChanged(action.name)
            is CreditCardItemEditAction.SpaceChanged -> onSpaceChanged(action.space)
            is CreditCardItemEditAction.ColorChanged -> colorChanged(action.colorId)
            is CreditCardItemEditAction.RevealHideCardNumber -> onRevealHideCardNumber()
            is CreditCardItemEditAction.RevealHideSecurityCode -> onRevealHideSecurityCode()
            is CreditCardItemEditAction.RevealHideNote -> onRevealHideNote()
            is CreditCardItemEditAction.CopyCardHolder -> onCopyCardHolder()
            is CreditCardItemEditAction.CopyCardNumber -> onCopyCardNumber()
            is CreditCardItemEditAction.CopySecurityCode -> onCopySecurityCode()
            is CreditCardItemEditAction.CopyNote -> onCopyNote()
            is CreditCardItemEditAction.Delete -> onDelete()
            is CreditCardItemEditAction.OnNfcCardRead -> onNfcCardRead(action.tag)
        }
    }

    private fun onCardHolderChanged(value: String) {
        viewModelScope.launch(defaultDispatcher) {
            mutableUiState.update { state ->
                state.copyCurrentFormData {
                    it.copy(cardHolder = value.takeIf { it.isNotBlank() })
                }
            }
        }
    }

    private fun onCardNumberChanged(value: String) {
        viewModelScope.launch(defaultDispatcher) {
            if (value.any { it.isDigit().not() }) return@launch

            mutableUiState.update { state ->
                state.copyCurrentFormData {
                    it.copy(
                        cardNumberFormat = creditCardTypeFormatBuilder.fromNumber(value),
                        cardNumber = value.takeIf { it.isNotBlank() },
                        isCardNumberEmpty = value.isBlank(),
                        cardNumberLength = value.length,
                    )
                }
            }
        }
    }

    private fun onExpirationDateChanged(value: Long) {
        viewModelScope.launch(defaultDispatcher) {
            val date = LocalDate.ofInstant(
                Instant.ofEpochMilli(value),
                ZoneId.of("UTC"),
            )
            val lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth())

            mutableUiState.update { state ->
                state.copyCurrentFormData {
                    it.copy(
                        expirationMonth = lastDayOfMonth.monthValue,
                        expirationYear = lastDayOfMonth.year,
                    )
                }
            }
        }
    }

    private fun itemNameChanged(name: String) {
        viewModelScope.launch(defaultDispatcher) {
            mutableUiState.update { state ->
                state.copyCurrentCommonData {
                    it.copy(name = name.takeIf { it.isNotSemanticallyNull() })
                }
            }
        }
    }

    private fun onSpaceChanged(space: TeamSpace) {
        viewModelScope.launch(defaultDispatcher) {
            updateSpace(space)
        }
    }

    private fun colorChanged(colorId: String) {
        viewModelScope.launch(defaultDispatcher) {
            mutableUiState.update { state ->
                val color = SyncObject.PaymentCreditCard.Color.entries
                    .firstOrNull { it.value == colorId } ?: return@launch

                state.copyCurrentFormData {
                    it.copy(
                        colorId = colorId,
                        color = color.getColorResource(),
                    )
                }
            }
        }
    }

    private fun onSecurityCodeChanged(value: String) {
        viewModelScope.launch(defaultDispatcher) {
            if (value.any { it.isDigit().not() }) return@launch

            mutableUiState.update { state ->
                state.copyCurrentFormData {
                    it.copy(
                        securityCode = value.takeIf { it.isNotBlank() },
                        isSecurityCodeEmpty = value.isBlank(),
                    )
                }
            }
        }
    }

    private fun onNoteChanged(value: String) {
        viewModelScope.launch(defaultDispatcher) {
            mutableUiState.update { state ->
                state.copyCurrentFormData {
                    it.copy(
                        note = value.takeIf { it.isNotBlank() },
                        isNoteEmpty = value.isBlank(),
                    )
                }
            }
        }
    }

    private fun onRevealHideCardNumber() {
        viewModelScope.launch(defaultDispatcher) {
            val state = mutableUiState.value
            if (state.revealedFields.contains(SensitiveField.CREDIT_CARD_NUMBER)) {
                hideSensitiveField(SensitiveField.CREDIT_CARD_NUMBER)
            } else {
                showSensitiveField(SensitiveField.CREDIT_CARD_NUMBER)
            }
        }
    }

    private fun onRevealHideNote() {
        viewModelScope.launch(defaultDispatcher) {
            val state = mutableUiState.value
            if (state.revealedFields.contains(SensitiveField.CREDIT_CARD_NOTE)) {
                hideSensitiveField(SensitiveField.CREDIT_CARD_NOTE)
            } else {
                showSensitiveField(SensitiveField.CREDIT_CARD_NOTE)
            }
        }
    }

    private fun onRevealHideSecurityCode() {
        viewModelScope.launch(defaultDispatcher) {
            val state = mutableUiState.value
            if (state.revealedFields.contains(SensitiveField.CREDIT_CARD_SECURITY_CODE)) {
                hideSensitiveField(SensitiveField.CREDIT_CARD_SECURITY_CODE)
            } else {
                showSensitiveField(SensitiveField.CREDIT_CARD_SECURITY_CODE)
            }
        }
    }

    private fun onCopyCardHolder() {
        copyToClipboard(CopyField.PaymentsCardHolder)
    }

    private fun onCopyCardNumber() {
        copyToClipboard(CopyField.PaymentsNumber)
    }

    private fun onCopySecurityCode() {
        copyToClipboard(CopyField.PaymentsSecurityCode)
    }

    private fun onCopyNote() {
        copyToClipboard(CopyField.PaymentsCardNote)
    }

    private fun onDelete() {
        super.actionDelete()
    }

    private fun onNfcCardRead(tag: Tag) {
        if (mutableUiState.value.isEditMode.not()) return

        viewModelScope.launch(defaultDispatcher) {
            val card = nfcCreditCardReader.readTag(tag)
            if (card?.cardNumber != null) {
                handleAction(CreditCardItemEditAction.CardNumberChanged(card.cardNumber))
            }

            if (card?.expireDate != null) {
                
                val date = LocalDate.ofInstant(
                    Instant.ofEpochMilli(card.expireDate.time),
                    ZoneId.of("UTC"),
                ).plusDays(1)
                    .atStartOfDay(ZoneId.of("UTC"))
                    .toInstant()
                    .toEpochMilli()

                handleAction(CreditCardItemEditAction.ExpirationDateChanged(date))
            }

            if (card?.holderFirstname != null || card?.holderLastname != null) {
                val holderName = listOfNotNull(
                    card.holderFirstname,
                    card.holderLastname,
                ).joinToString(" ")

                handleAction(CreditCardItemEditAction.CardHolderChanged(holderName))
            }

            if (card?.cardNumber != null) {
                mutableUiState.update {
                    it.copy(dialog = CreditCardDialog.NfcReaderSucceed)
                }
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                mutableUiState.send(ItemEditSideEffect.CreditCardSecurityCodeRequestFocus)
            } else {
                mutableUiState.update {
                    it.copy(dialog = CreditCardDialog.NfcReaderFailed)
                }

                mutableUiState.send(ItemEditSideEffect.CreditCardNumberRequestFocus)
            }
        }
    }
}

sealed class CreditCardItemEditAction {
    data class CardHolderChanged(
        val value: String,
    ) : CreditCardItemEditAction()

    data class CardNumberChanged(
        val value: String,
    ) : CreditCardItemEditAction()

    data class SecurityCodeChanged(
        val value: String,
    ) : CreditCardItemEditAction()

    data class ExpirationDateChanged(
        val value: Long,
    ) : CreditCardItemEditAction()

    data class NoteChanged(
        val value: String,
    ) : CreditCardItemEditAction()

    data class ItemNameChanged(
        val name: String,
    ) : CreditCardItemEditAction()

    data class SpaceChanged(
        val space: TeamSpace,
    ) : CreditCardItemEditAction()

    data class ColorChanged(
        val colorId: String,
    ) : CreditCardItemEditAction()

    data class OnNfcCardRead(val tag: Tag) : CreditCardItemEditAction()

    data object RevealHideCardNumber : CreditCardItemEditAction()

    data object RevealHideSecurityCode : CreditCardItemEditAction()

    data object RevealHideNote : CreditCardItemEditAction()

    data object CopyCardHolder : CreditCardItemEditAction()

    data object CopyCardNumber : CreditCardItemEditAction()

    data object CopySecurityCode : CreditCardItemEditAction()

    data object CopyNote : CreditCardItemEditAction()

    data object Delete : CreditCardItemEditAction()
}