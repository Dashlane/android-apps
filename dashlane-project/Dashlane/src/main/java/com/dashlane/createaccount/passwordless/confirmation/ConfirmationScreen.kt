package com.dashlane.createaccount.passwordless.confirmation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dashlane.R
import com.dashlane.createaccount.passwordless.MplessAccountCreationViewModel
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import kotlinx.coroutines.delay

@Composable
fun ConfirmationScreen(
    modifier: Modifier = Modifier,
    mpLessViewModel: MplessAccountCreationViewModel,
    viewModel: ConfirmationScreenViewModel,
    onAccountCreated: () -> Unit,
    onErrorMessageToDisplay: (Int) -> Unit,
    onApplicationVersionExpired: () -> Unit
) {
    BackHandler(enabled = true) {
        
        
    }

    val uiState by viewModel.uiState.collectAsState()
    val userDataState by mpLessViewModel.userDataStateFlow.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.createAccount(userDataState)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ConfirmationState.AccountCreated -> {
                delay(2_000)
                onAccountCreated()
                viewModel.hasNavigated()
            }

            is ConfirmationState.Error.NetworkError -> onErrorMessageToDisplay((uiState as ConfirmationState.Error).messageRes)
            is ConfirmationState.Error.ExpiredVersion -> onApplicationVersionExpired()
            is ConfirmationState.Error.UnknownError -> onErrorMessageToDisplay((uiState as ConfirmationState.Error).messageRes)

            else -> Unit
        }
    }
    ConfirmationContent(
        modifier = modifier,
        isLoading = (uiState !is ConfirmationState.AccountCreated)
    )
}

@Composable
fun ConfirmationContent(
    modifier: Modifier,
    isLoading: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        LoaderComposable(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f),
            hasFinishedLoading = !isLoading,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
    }
}

@Composable
fun LoaderComposable(
    modifier: Modifier,
    hasFinishedLoading: Boolean,
    verticalArrangement: Arrangement.Vertical,
    horizontalAlignment: Alignment.Horizontal
) {
    val loading by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_indeterminate))
    val success by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading_success))

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        var isTextVisible by remember { mutableStateOf(false) }

        LaunchedEffect(hasFinishedLoading) {
            if (hasFinishedLoading) {
                isTextVisible = true
            }
        }
        if (hasFinishedLoading) {
            LottieAnimation(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                composition = success,
                iterations = 1
            )

            AnimatedVisibility(
                visible = isTextVisible,
                enter = fadeIn(
                    
                    initialAlpha = 0.3f
                )
            ) {
                Text(
                    text = stringResource(id = R.string.passwordless_account_creation_confirmed_title),
                    style = DashlaneTheme.typography.titleSectionLarge,
                    color = DashlaneTheme.colors.textNeutralCatchy,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                )
            }
        } else {
            LottieAnimation(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                composition = loading,
                iterations = LottieConstants.IterateForever
            )
        }
    }
}

@Preview
@Composable
fun ConfirmationContentPreview() {
    DashlanePreview {
        ConfirmationContent(
            modifier = Modifier,
            isLoading = true
        )
    }
}