package com.dashlane.pin.setup

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.pin.setup.PinSetupViewModel.Companion.PIN_LENGTH
import com.dashlane.ui.common.compose.components.pincode.PinKeyboard
import com.dashlane.ui.common.compose.components.pincode.PinTextField
import com.dashlane.util.animation.shake

@Composable
fun PinSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: PinSetupViewModel,
    isCancellable: Boolean,
    onPinChosen: (String) -> Unit,
    onCancel: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle(null)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> viewModel.onViewResumed(isCancellable)
            else -> Unit
        }
    }

    LaunchedEffect(navigationState) {
        when (val state = navigationState) {
            PinSetupNavigationState.Cancel -> onCancel?.let { it() }
            is PinSetupNavigationState.GoToNext -> onPinChosen(state.pinCode)
            is PinSetupNavigationState.GoToSystemLockSetting -> context.startActivity(state.intent)
            null -> Unit
        }
    }

    val title = when (uiState.data.confirming) {
        true -> stringResource(id = R.string.pin_setup_screen_confirm)
        false -> stringResource(R.string.pin_setup_screen_choose)
    }

    PinContent(
        modifier = modifier,
        title = title,
        pinCode = uiState.data.pinCode,
        onPinUpdated = viewModel::onPinUpdated,
        onCancel = viewModel::cancel,
        isCancellable = uiState.data.isCancellable,
        isError = (uiState as? PinSetupState.PinUpdated)?.hasError == true
    )

    if (!uiState.data.isSystemLockSetup) {
        SystemLockSetupDialog(onConfirm = viewModel::onGoToSystemLockSetting)
    }
}

@Composable
fun PinContent(
    modifier: Modifier = Modifier,
    title: String,
    pinCode: String,
    isCancellable: Boolean,
    isError: Boolean,
    onPinUpdated: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val showPinKeyboard = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = title,
            style = DashlaneTheme.typography.titleSectionLarge,
            textAlign = TextAlign.Center
        )
        PinTextField(
            modifier = Modifier
                .padding(top = 32.dp, bottom = 16.dp)
                .shake(isError),
            length = PIN_LENGTH,
            value = pinCode,
            onValueChange = onPinUpdated,
            isError = isError,
            errorMessage = null
        )
        if (showPinKeyboard) {
            PinKeyboard(
                modifier = Modifier
                    .padding(bottom = 32.dp),
                value = pinCode,
                onValueChange = onPinUpdated
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isCancellable) {
            ButtonMedium(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                onClick = onCancel,
                intensity = Intensity.Supershy,
                layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.cancel))
            )
        }
    }
}

@Composable
fun SystemLockSetupDialog(
    onConfirm: () -> Unit
) {
    Dialog(
        title = stringResource(id = R.string.settings_use_pincode_need_screen_lock_title),
        description = {
            Text(text = stringResource(id = R.string.settings_use_pincode_need_screen_lock_description))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.settings_use_pincode_need_screen_lock_action_settings)),
        mainActionClick = onConfirm,
        onDismissRequest = { },
        isDestructive = false
    )
}

@Preview
@Composable
private fun PinScreenPreview() {
    DashlanePreview {
        PinContent(
            title = stringResource(R.string.pin_dialog_set_topic),
            pinCode = "000",
            isError = false,
            isCancellable = true,
            onPinUpdated = {},
            onCancel = {}
        )
    }
}

@Preview
@Composable
private fun SystemLockSetupDialogPreview() {
    DashlanePreview {
        SystemLockSetupDialog(
            onConfirm = {}
        )
    }
}
