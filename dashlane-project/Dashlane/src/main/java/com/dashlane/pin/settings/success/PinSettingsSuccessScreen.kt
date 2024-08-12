package com.dashlane.pin.settings.success

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.tooling.DashlanePreview
import kotlinx.coroutines.delay

@Composable
@Suppress("LongMethod")
fun PinSettingsSuccessScreen(
    viewModel: PinSettingSuccessViewModel,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle(null)

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
    }

    LaunchedEffect(navigationState) {
        when (navigationState) {
            PinSettingsSuccessNavigationState.Cancel -> onCancel()
            PinSettingsSuccessNavigationState.Success -> {
                delay(500) 
                onSuccess()
            }
            else -> Unit
        }
    }

    BackHandler(enabled = true) {
        if (navigationState is PinSettingsSuccessNavigationState.Success) {
            onSuccess()
        } else {
            viewModel.cancel()
        }
    }

    PinSettingSuccessContent(
        isSuccess = navigationState is PinSettingsSuccessNavigationState.Success,
        isMPStoreDialogShown = uiState.isMPStoreDialogShown,
        onDialogPositiveButtonClick = viewModel::onContinue,
        onDialogNegativeButtonClick = viewModel::cancel
    )
}

@Composable
fun PinSettingSuccessContent(
    modifier: Modifier = Modifier,
    isSuccess: Boolean,
    isMPStoreDialogShown: Boolean,
    onDialogPositiveButtonClick: () -> Unit,
    onDialogNegativeButtonClick: () -> Unit,
) {
    val loading by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_indeterminate))
    val success by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_success))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        if (isSuccess) {
            LottieAnimation(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                composition = success,
            )
        } else {
            LottieAnimation(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                composition = loading,
                iterations = LottieConstants.IterateForever
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }

    if (isMPStoreDialogShown) {
        StoreMPConfirmationDialog(
            onPositiveButtonClick = onDialogPositiveButtonClick,
            onNegativeButtonClick = onDialogNegativeButtonClick
        )
    }
}

@Composable
fun StoreMPConfirmationDialog(
    onPositiveButtonClick: () -> Unit,
    onNegativeButtonClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onNegativeButtonClick,
        title = stringResource(id = R.string.settings_use_pincode),
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.ok)),
        mainActionClick = onPositiveButtonClick,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cancel)),
        additionalActionClick = onNegativeButtonClick
    ) {
        Text(text = stringResource(id = R.string.pin_setup_store_mp_warning_dialog))
    }
}

@Preview
@Composable
fun PinSettingSuccessContentPreview() {
    DashlanePreview {
        PinSettingSuccessContent(
            isSuccess = false,
            isMPStoreDialogShown = false,
            onDialogPositiveButtonClick = {},
            onDialogNegativeButtonClick = {},
        )
    }
}

@Preview
@Composable
fun StoreMPConfirmationDialogPreview() {
    DashlanePreview {
        StoreMPConfirmationDialog(
            onPositiveButtonClick = {},
            onNegativeButtonClick = {},
        )
    }
}
