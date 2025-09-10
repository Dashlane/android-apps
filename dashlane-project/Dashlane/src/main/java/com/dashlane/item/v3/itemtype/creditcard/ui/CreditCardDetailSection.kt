package com.dashlane.item.v3.itemtype.creditcard.ui

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.R
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.display.fields.GenericField
import com.dashlane.item.v3.display.fields.ItemEditDateField
import com.dashlane.item.v3.display.fields.ItemEditObfuscatedField
import com.dashlane.item.v3.display.fields.ItemEditObfuscatedTextArea
import com.dashlane.item.v3.display.fields.SectionContent
import com.dashlane.item.v3.display.fields.SectionTitle
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.itemtype.creditcard.CreditCardItemEditAction
import com.dashlane.item.v3.util.Field
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.util.rememberIsFieldVisible
import com.dashlane.item.v3.util.rememberIsSensitiveFieldRevealed
import com.dashlane.item.v3.viewmodels.Data

@Suppress(
    "LongMethod",
    
    "kotlin:S3776",
)
@Composable
fun CreditCardDetailSection(
    data: Data<CreditCardFormData>,
    hiddenFields: Set<Field>,
    revealedFields: Set<SensitiveField>,
    editMode: Boolean,
    numberFocusRequested: FocusRequester,
    securityCodeFocusRequested: FocusRequester,
    handleAction: (CreditCardItemEditAction) -> Unit,
) {
    val expirationDateFocusRequester = remember { FocusRequester() }

    SectionContent(showCard = !editMode) {
        SectionTitle(
            title = stringResource(id = R.string.credit_card_detail_section),
            editMode = editMode,
        )
        if (rememberIsFieldVisible(hiddenFields, Field.CREDIT_CARD_OWNER_NAME)) {
            GenericField(
                label = stringResource(id = R.string.credit_card_hint_account_holder),
                data = data.formData.cardHolder,
                editMode = editMode,
                isEditable = data.commonData.isEditable,
                onValueChanged = { handleAction(CreditCardItemEditAction.CardHolderChanged(it)) },
                onValueCopy = { handleAction(CreditCardItemEditAction.CopyCardHolder) }
                    .takeIf { data.commonData.isCopyActionAllowed },
                imeAction = ImeAction.Next,
            )
        }
        ItemEditObfuscatedField(
            modifier = Modifier.focusRequester(numberFocusRequested),
            label = stringResource(id = R.string.credit_card_hint_number),
            value = remember(data.formData.cardNumber) {
                data.formData.cardNumber ?: ""
            },
            isEmpty = data.formData.isCardNumberEmpty,
            onValueChange = { handleAction(CreditCardItemEditAction.CardNumberChanged(it)) },
            onHideReveal = { handleAction(CreditCardItemEditAction.RevealHideCardNumber) },
            onCopy = { handleAction(CreditCardItemEditAction.CopyCardNumber) }
                .takeIf { data.commonData.isCopyActionAllowed },
            readOnly = !data.commonData.isEditable,
            obfuscated = rememberIsSensitiveFieldRevealed(
                revealedFields = revealedFields,
                field = SensitiveField.CREDIT_CARD_NUMBER,
            ),
            fixedHiddenValueLength = data.formData.cardNumberLength,
            editMode = editMode,
            isRequired = true,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Next,
            readModeValueTransformation = remember(data.formData.cardNumberFormat) {
                if (data.formData.cardNumberFormat != null) {
                    paymentFormatCardNumberTransformation(data.formData.cardNumberFormat)
                } else {
                    null
                }
            },
        )
        if (rememberIsFieldVisible(hiddenFields, Field.CREDIT_CARD_SECURITY_CODE)) {
            ItemEditObfuscatedField(
                modifier = Modifier.focusRequester(securityCodeFocusRequested),
                label = stringResource(id = R.string.credit_card_hint_security_code),
                value = data.formData.securityCode ?: "",
                isEmpty = data.formData.isSecurityCodeEmpty,
                onValueChange = { handleAction(CreditCardItemEditAction.SecurityCodeChanged(it)) },
                onHideReveal = { handleAction(CreditCardItemEditAction.RevealHideSecurityCode) },
                onCopy = { handleAction(CreditCardItemEditAction.CopySecurityCode) }
                    .takeIf { data.commonData.isCopyActionAllowed },
                readOnly = !data.commonData.isEditable,
                obfuscated = rememberIsSensitiveFieldRevealed(
                    revealedFields = revealedFields,
                    field = SensitiveField.CREDIT_CARD_SECURITY_CODE,
                ),
                fixedHiddenValueLength = 3,
                editMode = editMode,
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { expirationDateFocusRequester.requestFocus() },
                ),
            )
        }
        if (rememberIsFieldVisible(hiddenFields, Field.CREDIT_CARD_EXPIRATION_DATE)) {
            ItemEditDateField(
                modifier = Modifier.focusRequester(expirationDateFocusRequester),
                label = stringResource(id = R.string.credit_card_hint_expiration_date),
                year = data.formData.expirationYear,
                month = data.formData.expirationMonth,
                editMode = editMode,
                isEditable = data.commonData.isEditable,
                onDateChange = { date ->
                    if (date != null) {
                        handleAction(CreditCardItemEditAction.ExpirationDateChanged(date))
                    }
                },
            )
        }
        if (rememberIsFieldVisible(hiddenFields, Field.CREDIT_CARD_NOTE)) {
            ItemEditObfuscatedTextArea(
                label = stringResource(id = R.string.credit_card_hint_note),
                value = data.formData.note ?: "",
                isEmpty = data.formData.isNoteEmpty,
                onValueChange = { handleAction(CreditCardItemEditAction.NoteChanged(it)) },
                onHideReveal = { handleAction(CreditCardItemEditAction.RevealHideNote) },
                onCopy = { handleAction(CreditCardItemEditAction.CopyNote) }
                    .takeIf { data.commonData.isCopyActionAllowed },
                readOnly = !data.commonData.isEditable,
                obfuscated = rememberIsSensitiveFieldRevealed(
                    revealedFields = revealedFields,
                    field = SensitiveField.CREDIT_CARD_NOTE,
                ),
                fixedHiddenValueLength = 4,
                editMode = editMode,
                imeAction = ImeAction.Next,
            )
        }
    }
}

@Preview
@Composable
private fun CreditCardDetailSectionPreview() = DashlanePreview {
    CreditCardDetailSection(
        data = Data(
            commonData = CommonData(
                isEditable = false,
            ),
            formData = CreditCardFormData(
                cardHolder = "John Doe",
                cardNumber = "xxxx xxxx xxxx 3456",
                securityCode = "***",
                expirationYear = 2024,
                expirationMonth = 11,
                colorId = "BLUE",
                color = R.color.credit_card_blue1,
            ),
        ),
        hiddenFields = emptySet(),
        revealedFields = emptySet(),
        editMode = true,
        handleAction = {},
        numberFocusRequested = FocusRequester(),
        securityCodeFocusRequested = FocusRequester(),
    )
}

@Preview
@Composable
private fun CreditCardDetailSectionEditModePreview() = DashlanePreview {
    CreditCardDetailSection(
        data = Data(
            commonData = CommonData(
                isEditable = true,
            ),
            formData = CreditCardFormData(
                cardHolder = "John Doe",
                cardNumber = "123456789123456",
                securityCode = "***",
                expirationYear = 2024,
                expirationMonth = 11,
                colorId = "BLUE",
            ),
        ),
        hiddenFields = emptySet(),
        revealedFields = emptySet(),
        editMode = true,
        handleAction = {},
        numberFocusRequested = FocusRequester(),
        securityCodeFocusRequested = FocusRequester(),
    )
}