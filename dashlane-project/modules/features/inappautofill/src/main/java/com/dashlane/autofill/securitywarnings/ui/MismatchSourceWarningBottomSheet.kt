package com.dashlane.autofill.securitywarnings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.autofill.api.R
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.securitywarnings.MismatchSourceState
import com.dashlane.autofill.securitywarnings.MismatchSourceViewModel
import com.dashlane.autofill.ui.AutofillCard
import com.dashlane.autofill.ui.DashlaneAutofillDecoration
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.DisplayField
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.ui.common.compose.components.CheckboxWithLabel
import com.dashlane.ui.common.compose.components.StatusBarBackground
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon

@Composable
internal fun MismatchSourceWarningBottomSheet(
    viewModel: MismatchSourceViewModel,
    onFillCancelled: (ApplicationFormSource?) -> Unit,
    onFillAllowed: (ApplicationFormSource?, Boolean) -> Unit,
    onFillDenied: (ApplicationFormSource?) -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is MismatchSourceState.SideEffect.OnFillAllowed ->
                    onFillAllowed(sideEffect.applicationFormSource, sideEffect.doNotShowAgainChecked)

                is MismatchSourceState.SideEffect.OnFillCancelled ->
                    onFillCancelled(sideEffect.applicationFormSource)

                is MismatchSourceState.SideEffect.OnFillDenied ->
                    onFillDenied(sideEffect.applicationFormSource)
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    Box {
        StatusBarBackground()
        MismatchSourceWarningBottomSheet(
            credentialTitle = uiState.credentialTitle,
            credentialUrl = uiState.credentialUrl,
            credentialLogin = uiState.credentialLogin,
            applicationName = uiState.applicationName,
            doNotShowAgainChecked = uiState.doNotShowAgainChecked,
            onDoNotShowAgainToggleChange = viewModel::onDoNotShowAgainToggleChange,
            onDismissRequest = viewModel::onFillCancelled,
            onAllowClick = viewModel::onFillAllowed,
            onCancelClick = viewModel::onFillDenied,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("kotlin:S107")
@Composable
internal fun MismatchSourceWarningBottomSheet(
    credentialTitle: String,
    credentialUrl: String?,
    credentialLogin: String,
    applicationName: String,
    doNotShowAgainChecked: Boolean,
    onDoNotShowAgainToggleChange: () -> Unit,
    onAllowClick: (Boolean) -> Unit,
    onCancelClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismissRequest,
    ) {
        MismatchSourceWarningBottomSheetContent(
            credentialTitle = credentialTitle,
            credentialUrl = credentialUrl,
            credentialLogin = credentialLogin,
            applicationName = applicationName,
            doNotShowAgainChecked = doNotShowAgainChecked,
            onDoNotShowAgainToggleChange = onDoNotShowAgainToggleChange,
            onAllowClick = onAllowClick,
            onCancelClick = onCancelClick,
        )
    }
}

@Suppress("kotlin:S107")
@Composable
private fun MismatchSourceWarningBottomSheetContent(
    credentialTitle: String,
    credentialUrl: String?,
    credentialLogin: String,
    applicationName: String,
    doNotShowAgainChecked: Boolean,
    onDoNotShowAgainToggleChange: () -> Unit,
    onAllowClick: (Boolean) -> Unit,
    onCancelClick: () -> Unit,
) {
    BottomSheetContent(
        decoration = {
            DashlaneAutofillDecoration()
        },
        title = stringResource(R.string.autofill_mismatch_warning_title, credentialTitle, applicationName),
        description = stringResource(R.string.autofill_unknown_warning_message),
        extraContent = {
            AutofillCard(
                modifier = Modifier.fillMaxWidth(),
                listItemHeader = {
                    ListItem(
                        thumbnail = {
                            val thumbnailDescription = stringResource(R.string.datatype_authentifiant)
                            ThumbnailDomainIcon(
                                modifier = Modifier.semantics {
                                    contentDescription = thumbnailDescription
                                },
                                urlDomain = credentialUrl,
                            )
                        },
                        title = credentialTitle,
                        description = credentialLogin,
                        onClick = null,
                    )
                },
            ) {
                DisplayField(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    label = stringResource(R.string.autofill_mismatch_warning_app_label),
                    value = applicationName,
                    actions = null,
                )
            }
            Spacer(
                modifier = Modifier.height(16.dp),
            )
            CheckboxWithLabel(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.autofill_warning_do_not_show_again),
                checked = doNotShowAgainChecked,
                onClick = onDoNotShowAgainToggleChange,
            )
        },
        actions = BottomSheetActions(
            primaryButtonLayout = ButtonLayout.TextOnly(stringResource(R.string.autofill_unknown_warning_accept)),
            onPrimaryButtonClick = { onAllowClick(doNotShowAgainChecked) },
            secondaryButtonLayout = ButtonLayout.TextOnly(stringResource(R.string.autofill_warning_cancel)),
            onSecondaryButtonClick = onCancelClick,
        ),
    )
}

@Preview
@Composable
private fun MismatchSourceWarningBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        var checked by remember { mutableStateOf(false) }
        MismatchSourceWarningBottomSheetContent(
            credentialUrl = "discord.com",
            credentialLogin = "randomemail@provider.com",
            credentialTitle = "Discord",
            applicationName = "Disney",
            doNotShowAgainChecked = checked,
            onDoNotShowAgainToggleChange = { checked = !checked },
            onAllowClick = {},
            onCancelClick = {},
        )
    }
}