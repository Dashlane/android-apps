package com.dashlane.createaccount.passwordless.pincodesetup

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.R
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.widgets.compose.pincode.PinKeyboard
import com.dashlane.ui.widgets.compose.pincode.PinTextField

@Composable
fun PinSetupScreen(
    viewModel: PinSetupViewModel,
    onPinChosen: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.onViewStarted()
    }

    LaunchedEffect(uiState) {
        if (uiState is PinSetupState.GoToNext) {
            viewModel.hasNavigated()
            onPinChosen(uiState.pinCode)
        }
    }

    PinContent(
        title = if (uiState is PinSetupState.Choose) {
            stringResource(R.string.pin_setup_screen_choose)
        } else {
            stringResource(id = R.string.pin_setup_screen_confirm)
        },
        pinCode = uiState.pinCode,
        onPinUpdated = viewModel::onPinUpdated,
        errorMessage = if ((uiState as? PinSetupState.Choose)?.hasError == true) stringResource(id = R.string.error) else null
    )
}

@Composable
fun PinContent(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    pinCode: String,
    errorMessage: String?,
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
        if (subTitle != null) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = subTitle,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                textAlign = TextAlign.Center
            )
        }

        BoxWithConstraints {
            val showPinKeyboard = maxHeight > 500.dp
            CompositionLocalProvider(
                LocalTextInputService provides LocalTextInputService.current.takeUnless { showPinKeyboard }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PinTextField(
                        modifier = modifier.padding(top = 32.dp),
                        value = pinCode,
                        onValueChange = onPinUpdated,
                        errorMessage = errorMessage
                    )
                    if (showPinKeyboard) {
                        PinKeyboard(
                            modifier = modifier.padding(top = 16.dp),
                            value = pinCode,
                            onValueChange = onPinUpdated
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PinScreenPreview() {
    DashlanePreview {
        PinContent(
            title = stringResource(R.string.pin_dialog_set_topic),
            subTitle = stringResource(R.string.pin_dialog_set_question),
            pinCode = "000",
            errorMessage = stringResource(id = R.string.error),
            onPinUpdated = {}
        )
    }
}
