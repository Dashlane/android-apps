package com.dashlane.wear.vault.details

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.authenticator.Totp
import com.dashlane.design.component.resetCountdown
import com.dashlane.design.component.startCountdown
import com.dashlane.wear.R
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.screen.WearContentWrapper
import com.dashlane.wear.ui.screen.WearGenericLoadingScreen
import com.dashlane.wear.ui.screen.WearTextContentScreen
import com.dashlane.wear.vault.WearVaultItem
import kotlinx.coroutines.delay

@Composable
fun WearItemDetailsScreen(
    viewModel: WearItemDetailViewModel,
    itemId: String,
) {
    LaunchedEffect(viewModel) {
        viewModel.loadItem(itemId)
    }

    val uiState = viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    when (val viewState = uiState.value) {
        WearItemDetailsState.View.Loading -> WearGenericLoadingScreen()
        is WearItemDetailsState.View.Ready -> WearItemDetailsContent(item = viewState.item)
        WearItemDetailsState.View.Error -> WearTextContentScreen(text = "Cannot load Item")
    }
}

@Composable
private fun WearItemDetailsContent(
    item: WearVaultItem,
    modifier: Modifier = Modifier,
) {
    val scalingListState = rememberScalingLazyListState(initialCenterItemIndex = 0, initialCenterItemScrollOffset = 0)
    WearContentWrapper(
        isLogoVisible = true,
        scrollableState = scalingListState,
        positionIndicator = {
            PositionIndicator(scalingLazyListState = scalingListState)
        },
    ) { contentPadding ->
        ScalingLazyColumn(
            modifier = modifier
                .padding(horizontal = 16.dp),
            autoCentering = null,
            state = scalingListState,
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = 48.dp,
            ),
        ) {
            item(key = item.id) {
                WearItemDetails(
                    modifier = Modifier.fillMaxWidth(),
                    urlDomain = item.urlForUi,
                    title = item.title,
                    fields = listOfNotNull(
                        ItemField(
                            title = stringResource(R.string.item_details_passkey_username),
                            content = item.username,
                        ),
                        otpItemField(item.totp),
                        ItemField(
                            title = stringResource(R.string.item_details_passkey_website),
                            content = item.url,
                        ),
                    ),
                )
            }
        }
    }
}

@Composable
private fun otpItemField(totp: Totp?): ItemField? {
    if (totp == null) return null
    val pin = totp.getPin() ?: return null
    var token by rememberSaveable { mutableStateOf(pin.code) }
    val animatedTimeRemaining = remember { Animatable(pin.timeRemaining.seconds.toFloat()) }
    LaunchedEffect(Unit) {
        while (true) {
            animatedTimeRemaining.startCountdown(animatedTimeRemaining.value.toInt())
            delay(animatedTimeRemaining.value.toInt() * 1_000L)
            val newPin = totp.getPin() ?: return@LaunchedEffect
            token = newPin.code
            animatedTimeRemaining.resetCountdown(newPin.refreshInterval)
        }
    }
    return ItemField(
        title = stringResource(R.string.item_details_password_otp),
        content = token.chunked(3).joinToString('\u205f'.toString()),
    )
}

@Composable
@WearPreviewSmallRound
private fun WearItemDetailsScreenPreview() {
    DashlaneWearPreview {
        WearItemDetailsContent(
            item = WearVaultItem(
                id = "passkeys.io",
                username = "randomemail@provider.com",
                title = "passkeys.io",
                url = "https://passkeys.io",
                urlForUi = "passkeys.io",
                isPasskey = true,
            ),
        )
    }
}