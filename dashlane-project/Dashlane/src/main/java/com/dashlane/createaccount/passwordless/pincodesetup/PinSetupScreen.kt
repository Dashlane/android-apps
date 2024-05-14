package com.dashlane.createaccount.passwordless.pincodesetup

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.pincode.PinKeyboard
import com.dashlane.ui.widgets.compose.pincode.PinTextField
import com.dashlane.util.animation.shake

@Composable
fun PinSetupScreen(
    modifier: Modifier = Modifier,
    viewModel: PinSetupViewModel,
    onPinChosen: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> viewModel.onViewResumed()
            else -> Unit
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is PinSetupState.GoToNext -> {
                viewModel.hasNavigated()
                onPinChosen(uiState.data.pinCode)
            }
            is PinSetupState.GoToSystemLockSetting -> {
                viewModel.hasNavigated()
                context.startActivity(state.intent)
            }
            else -> Unit
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
    isError: Boolean,
    onPinUpdated: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = title,
            style = DashlaneTheme.typography.titleSectionLarge,
            textAlign = TextAlign.Center
        )

        BoxWithConstraints {
            val showPinKeyboard = maxHeight > 500.dp
            CompositionLocalProvider(
                LocalTextInputService provides LocalTextInputService.current.takeUnless { showPinKeyboard }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PinTextField(
                        modifier = Modifier
                            .padding(top = 32.dp, bottom = 16.dp)
                            .shake(isError),
                        value = pinCode,
                        onValueChange = onPinUpdated,
                        isError = isError,
                        errorMessage = null
                    )
                    if (showPinKeyboard) {
                        PinKeyboard(
                            modifier = Modifier
                                .padding(bottom = 32.dp)
                                .fillMaxHeight(),
                            value = pinCode,
                            onValueChange = onPinUpdated
                        )
                    }
                }
            }
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
fun PinScreenPreview() {
    DashlanePreview {
        PinContent(
            title = stringResource(R.string.pin_dialog_set_topic),
            pinCode = "000",
            isError = false,
            onPinUpdated = {}
        )
    }
}

@Preview
@Composable
fun SystemLockSetupDialogPreview() {
    DashlanePreview {
        SystemLockSetupDialog(
            onConfirm = {}
        )
    }
}
