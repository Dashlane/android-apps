package com.dashlane.autofill.pause.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.autofill.api.R
import com.dashlane.autofill.pause.AskPauseState
import com.dashlane.autofill.pause.AskPauseViewModel
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.autofill.ui.AutofillCard
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.DropdownItem
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.ui.common.compose.components.StatusBarBackground

@Composable
internal fun AskPauseBottomSheetScreen(
    viewModel: AskPauseViewModel,
    onPauseSuccess: (String) -> Unit,
    onPauseError: (String) -> Unit,
    onPauseCancelled: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is AskPauseState.SideEffect.OnCancel -> onPauseCancelled()
                is AskPauseState.SideEffect.OnError -> onPauseError(sideEffect.message)
                is AskPauseState.SideEffect.OnSuccess -> onPauseSuccess(sideEffect.message)
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    when (val viewState = uiState) {
        AskPauseState.View.Initial -> {
            
        }
        is AskPauseState.View.Loaded -> {
            Box {
                StatusBarBackground()
                AskPauseBottomSheet(
                    title = viewState.viewData.title,
                    icon = viewState.viewData.icon,
                    serviceName = viewState.viewData.serviceName,
                    selectedPauseDuration = viewState.viewData.selectedPauseDuration,
                    onDismissRequest = viewModel::onDismiss,
                    onPauseOptionClick = viewModel::onPauseOptionClick,
                    onDoneClick = viewModel::onDoneClick,
                )
            }
        }
        AskPauseState.View.Hidden -> {
            
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AskPauseBottomSheet(
    title: String,
    icon: AutofillFormSourceIcon,
    serviceName: String,
    selectedPauseDuration: PauseDurations?,
    onDismissRequest: () -> Unit,
    onPauseOptionClick: (PauseDurations) -> Unit,
    onDoneClick: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismissRequest,
    ) {
        AskPauseBottomSheetContent(
            title = title,
            icon = icon,
            serviceName = serviceName,
            selectedPauseDuration = selectedPauseDuration,
            onPauseOptionClick = onPauseOptionClick,
            onDoneClick = onDoneClick,
        )
    }
}

@Composable
private fun AskPauseBottomSheetContent(
    title: String,
    icon: AutofillFormSourceIcon,
    serviceName: String,
    selectedPauseDuration: PauseDurations?,
    onPauseOptionClick: (PauseDurations) -> Unit,
    onDoneClick: () -> Unit,
) {
    BottomSheetContent(
        decoration = null,
        title = title,
        description = null,
        extraContent = {
            AutofillCard(
                listItemHeader = {
                    ListItem(
                        thumbnail = { AutofillFormSourceThumbnail(icon) },
                        title = serviceName,
                        description = null,
                        onClick = null,
                    )
                },
            ) {
                PauseOption(
                    duration = PauseDurations.ONE_HOUR,
                    selectedPauseDuration = selectedPauseDuration,
                    onPauseOptionClick = onPauseOptionClick,
                )
                PauseOption(
                    duration = PauseDurations.ONE_DAY,
                    selectedPauseDuration = selectedPauseDuration,
                    onPauseOptionClick = onPauseOptionClick,
                )
                PauseOption(
                    duration = PauseDurations.PERMANENT,
                    selectedPauseDuration = selectedPauseDuration,
                    onPauseOptionClick = onPauseOptionClick,
                )
            }
        },
        actions = BottomSheetActions(
            primaryButtonText = stringResource(R.string.autofill_ask_pause_action_done),
            onPrimaryButtonClick = onDoneClick,
            isPrimaryButtonEnabled = selectedPauseDuration != null,
            secondaryButtonText = null,
            onSecondaryButtonClick = null,
        ),
    )
}

@Composable
private fun PauseOption(
    duration: PauseDurations,
    selectedPauseDuration: PauseDurations?,
    modifier: Modifier = Modifier,
    onPauseOptionClick: (PauseDurations) -> Unit,
) {
    val text = when (duration) {
        PauseDurations.ONE_HOUR -> stringResource(R.string.autofill_do_not_show_again_pause_for_one_hour)
        PauseDurations.ONE_DAY -> stringResource(R.string.autofill_do_not_show_again_pause_for_one_day)
        PauseDurations.PERMANENT -> stringResource(R.string.autofill_do_not_show_again_pause_permanent)
    }
    DropdownItem(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(4.dp)),
        icon = null,
        text = text,
        selected = selectedPauseDuration == duration,
        onClick = { onPauseOptionClick(duration) },
    )
}

@Preview
@Composable
private fun AskPauseBottomSheetContentPreview() {
    DashlaneModalBottomSheetPreview {
        var selectedPauseDuration: PauseDurations? by remember { mutableStateOf(PauseDurations.ONE_HOUR) }
        AskPauseBottomSheetContent(
            title = "Pause Dashlane Autofill for",
            icon = AutofillFormSourceIcon.UnknownApplication,
            serviceName = "Sample",
            selectedPauseDuration = selectedPauseDuration,
            onPauseOptionClick = { option ->
                selectedPauseDuration = if (selectedPauseDuration == option) {
                    null
                } else {
                    option
                }
            },
            onDoneClick = {},
        )
    }
}