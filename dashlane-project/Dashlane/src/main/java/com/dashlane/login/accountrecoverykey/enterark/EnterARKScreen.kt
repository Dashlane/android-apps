package com.dashlane.login.accountrecoverykey.enterark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.Text
import com.dashlane.design.component.TextField
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyState
import com.dashlane.login.accountrecoverykey.LoginAccountRecoveryKeyViewModel
import com.dashlane.ui.widgets.compose.CircularProgressIndicator
import kotlinx.coroutines.delay

@Composable
fun EnterARKScreen(
    mainViewModel: LoginAccountRecoveryKeyViewModel,
    viewModel: EnterARKViewModel,
    goToTOTP: () -> Unit,
    goToToken: () -> Unit,
) {
    val mainState by mainViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is EnterARKState.GoToNext -> {
                viewModel.hasNavigated()
                mainViewModel.vaultKeyDecrypted(state.decryptedVaultKey)
            }

            else -> Unit
        }
    }

    when (mainState) {
        is LoginAccountRecoveryKeyState.GoToTOTP -> LaunchedEffect(mainState) { goToTOTP() }
        is LoginAccountRecoveryKeyState.GoToToken -> LaunchedEffect(mainState) { goToToken() }
        is LoginAccountRecoveryKeyState.GoToARK -> {
            when (val state = uiState) {
                is EnterARKState.Error,
                is EnterARKState.GoToNext,
                is EnterARKState.Initial,
                is EnterARKState.Loading -> {
                    EnterARKContent(
                        isLoading = uiState is EnterARKState.Loading,
                        isError = uiState is EnterARKState.Error,
                        accountRecoveryKey = "",
                        onNextClicked = { accountRecoveryKey ->
                            viewModel.onNextClicked(accountRecoveryKey, (mainState as LoginAccountRecoveryKeyState.GoToARK).authTicket)
                        }
                    )
                }
                is EnterARKState.KeyConfirmed -> EnterARKSuccessContent(onAnimationDone = { viewModel.animationEnded(state.decryptedVaultKey) })
            }
        }
        else -> Unit
    }
}

@Suppress("LongMethod")
@Composable
fun EnterARKContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isError: Boolean,
    accountRecoveryKey: String,
    onNextClicked: (String) -> Unit
) {
    var value by rememberSaveable { mutableStateOf(accountRecoveryKey) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            Text(
                text = stringResource(id = R.string.login_account_recovery_key_enter_step, 1, 3),
                style = DashlaneTheme.typography.titleSupportingSmall,
                color = DashlaneTheme.colors.textNeutralQuiet,
                modifier = Modifier
                    .padding(top = 72.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.login_account_recovery_key_enter_title),
                style = DashlaneTheme.typography.titleSectionLarge,
                color = DashlaneTheme.colors.textNeutralCatchy,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.login_account_recovery_key_enter_description),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier
                    .padding(bottom = 32.dp)
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                value = value,
                isError = isError,
                feedbackText = if (isError) stringResource(id = R.string.login_account_recovery_key_confirm_error) else null,
                onValueChange = { value = it },
                label = stringResource(id = R.string.login_account_recovery_key_enter_label)
            )
        }
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.End),
            onClick = { onNextClicked(value) },
            intensity = Intensity.Catchy,
            enabled = value.isNotEmpty(),
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.login_account_recovery_key_enter_button)
            )
        )
    }

    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun EnterARKSuccessContent(
    modifier: Modifier = Modifier,
    onAnimationDone: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_success))
    val animationState = animateLottieCompositionAsState(composition = composition, isPlaying = true)

    if (animationState.isAtEnd) {
        LaunchedEffect(Unit) {
            delay(500)
            onAnimationDone()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp),
            composition = composition,
            progress = { animationState.progress }
        )
        Text(
            text = stringResource(id = R.string.login_account_recovery_key_enter_success_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 32.dp)
        )
    }
}

@Preview
@Composable
fun EnterARKContentPreview() {
    DashlaneTheme {
        EnterARKContent(isLoading = true, isError = true, accountRecoveryKey = "accountRecoveryKey", onNextClicked = {})
    }
}
