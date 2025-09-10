package com.dashlane.autofill.frozenautofill

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dashlane.autofill.api.R
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.BottomSheetDefaults
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.common.compose.components.StatusBarBackground
import com.dashlane.ui.model.TextResource
import com.dashlane.ui.model.getText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrozenAutofillBottomSheet(
    state: FrozenAutofillState,
    onDismissRequest: () -> Unit,
    onCancelClick: () -> Unit,
    onUpgradeClick: () -> Unit,
) {
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = false,
    )

    LaunchedEffect(key1 = state.bottomSheetState) {
        when (state.bottomSheetState) {
            FrozenAutofillState.BottomSheetState.FOLDED -> bottomSheetState.hide()
            FrozenAutofillState.BottomSheetState.EXPANDED -> bottomSheetState.expand()
        }
    }

    Box {
        StatusBarBackground()
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = bottomSheetState,
        ) {
            FrozenAutofillContent(
                onCancelClick = onCancelClick,
                onUpgradeClick = onUpgradeClick,
                descriptionText = state.description,
            )
        }
    }
}

@Composable
internal fun FrozenAutofillContent(
    onCancelClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    descriptionText: TextResource,
) {
    BottomSheetContent(
        decoration = BottomSheetDefaults.decoration(IconTokens.premiumOutlined, Mood.Brand),
        title = stringResource(id = R.string.autofill_frozen_bottom_sheet_title),
        description = descriptionText.getText(),
        actions = BottomSheetActions(
            primaryButtonText = stringResource(id = R.string.autofill_frozen_bottom_sheet_cta_positive),
            onPrimaryButtonClick = onUpgradeClick,
            secondaryButtonText = stringResource(id = R.string.autofill_frozen_bottom_sheet_cta_negative),
            onSecondaryButtonClick = onCancelClick,
        ),
    )
}

@Preview
@Composable
private fun FrozenAutofillScreenPreview() = DashlanePreview {
    FrozenAutofillBottomSheet(
        state = FrozenAutofillState(
            description = TextResource.StringText(
                stringRes = R.string.autofill_frozen_bottom_sheet_description,
                arg = TextResource.Arg.StringArg("25"),
            ),
            bottomSheetState = FrozenAutofillState.BottomSheetState.EXPANDED,
        ),
        onDismissRequest = {},
        onCancelClick = {},
        onUpgradeClick = {},
    )
}