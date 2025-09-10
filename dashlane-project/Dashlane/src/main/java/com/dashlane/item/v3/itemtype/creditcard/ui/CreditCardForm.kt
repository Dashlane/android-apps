package com.dashlane.item.v3.itemtype.creditcard.ui

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import com.dashlane.R
import com.dashlane.item.v3.display.fields.DeleteItemField
import com.dashlane.item.v3.display.sections.OrganisationSection
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.itemtype.creditcard.CreditCardItemEditAction
import com.dashlane.item.v3.util.Field
import com.dashlane.item.v3.viewmodels.FormState

fun LazyListScope.creditCardForm(
    uiState: FormState<CreditCardFormData>,
    handleAction: (CreditCardItemEditAction) -> Unit,
    numberFocusRequested: FocusRequester,
    securityCodeFocusRequested: FocusRequester,
) {
    item(key = "CreditCardDetailSection") {
        CreditCardDetailSection(
            data = uiState.datas.current,
            editMode = uiState.isEditMode,
            hiddenFields = uiState.hiddenFields,
            revealedFields = uiState.revealedFields,
            handleAction = handleAction,
            numberFocusRequested = numberFocusRequested,
            securityCodeFocusRequested = securityCodeFocusRequested,
        )
    }
    if (uiState.isEditMode || uiState.hiddenFields.contains(Field.CREDIT_CARD_ITEM_NAME).not()) {
        item(key = "OrganisationSection") {
            OrganisationSection(
                data = uiState.datas.current,
                editMode = uiState.isEditMode,
                onItemNameChanged = {
                    handleAction(CreditCardItemEditAction.ItemNameChanged(it))
                },
                onSpaceChanged = {
                    handleAction(CreditCardItemEditAction.SpaceChanged(it))
                },
                onCreditCardColorChanged = {
                    handleAction(CreditCardItemEditAction.ColorChanged(it))
                },
            )
        }
    }
    item(key = "DeleteCreditCardField") {
        DeleteItemField(
            canDelete = uiState.datas.current.commonData.canDelete,
            editMode = uiState.isEditMode,
            label = stringResource(id = R.string.vault_delete_credit_card),
            onDeleteAction = { handleAction(CreditCardItemEditAction.Delete) },
        )
    }
}