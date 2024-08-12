package com.dashlane.autofill.frozenautofill

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.autofill.api.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.model.TextResource
import com.dashlane.ui.model.getText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrozenAutofillBottomSheet(
    state: FrozenAutofillState,
    onCancelClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        ),
    )

    LaunchedEffect(key1 = state.bottomSheetState) {
        when (state.bottomSheetState) {
            FrozenAutofillState.BottomSheetState.FOLDED -> bottomSheetScaffoldState.bottomSheetState.hide()
            FrozenAutofillState.BottomSheetState.EXPANDED -> bottomSheetScaffoldState.bottomSheetState.expand()
        }
    }

    BottomSheetScaffold(
        modifier = Modifier.fillMaxWidth(),
        sheetMaxWidth = 640.dp,
        sheetSwipeEnabled = false,
        sheetContainerColor = DashlaneTheme.colors.containerAgnosticNeutralSupershy,
        sheetDragHandle = null,
        sheetContent = {
            FrozenAutofillScreen(
                onCancelClick = onCancelClick,
                onUpgradeClick = onUpgradeClick,
                descriptionText = state.description
            )
        },
        scaffoldState = bottomSheetScaffoldState,
        content = {
            
        }
    )
}

@Composable
internal fun FrozenAutofillScreen(
    onCancelClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    descriptionText: TextResource,
) {
    val portrait =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val illustrationHeight = if (portrait) 180.dp else 90.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(illustrationHeight),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(id = R.drawable.illu_onboarding_vault),
                contentDescription = ""
            )
        }
        Text(
            modifier = Modifier.padding(top = 32.dp),
            text = stringResource(id = R.string.autofill_frozen_bottom_sheet_title),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy.value
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = descriptionText.getText(),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard.value
        )
        ButtonMediumBar(
            modifier = Modifier.padding(top = 32.dp),
            primaryButtonLayout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.autofill_frozen_bottom_sheet_cta_positive)
            ),
            onPrimaryButtonClick = onUpgradeClick,
            secondaryButtonLayout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.autofill_frozen_bottom_sheet_cta_negative)
            ),
            onSecondaryButtonClick = onCancelClick
        )
    }
}

@Preview
@Composable
private fun FrozenAutofillScreenPreview() = DashlanePreview {
    FrozenAutofillBottomSheet(
        state = FrozenAutofillState(
            description = TextResource.StringText(
                stringRes = R.string.autofill_frozen_bottom_sheet_description,
                arg = TextResource.Arg.StringArg("25")
            ),
            bottomSheetState = FrozenAutofillState.BottomSheetState.EXPANDED
        ),
        onCancelClick = {},
        onUpgradeClick = {},
    )
}
