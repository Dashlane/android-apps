package com.dashlane.wear.login.common.credentialmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.wear.login.R
import com.dashlane.wear.ui.DashlaneWearPreview
import com.dashlane.wear.ui.component.Emphasis
import com.dashlane.wear.ui.component.NumeratedTextList
import com.dashlane.wear.ui.component.WearChip
import com.dashlane.wear.ui.component.WearText
import com.dashlane.wear.ui.screen.WearContentWrapper

@Composable
fun WearSetupCredentialManagerScreen(
    viewModel: WearCredentialManagerViewModel,
    modifier: Modifier = Modifier,
    onSetupSuccess: () -> Unit,
) = WearSetupCredentialManagerContent(
    modifier = modifier,
    onSetupStarted = viewModel::enableCredentialProvider,
    onSetupSuccess = onSetupSuccess,
)

@Composable
fun WearSetupCredentialManagerContent(
    modifier: Modifier = Modifier,
    onSetupStarted: () -> Unit,
    onSetupSuccess: () -> Unit,
) {
    val listState = rememberScalingLazyListState(initialCenterItemIndex = 0)
    WearContentWrapper(
        isLogoVisible = true,
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        },
        scrollableState = listState,
    ) { contentPadding ->
        ScalingLazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
        ) {
            item(key = "CredentialManagerTitle") {
                WearText(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(R.string.credential_manager_header),
                    style = DashlaneTheme.typography.bodyReducedRegular.copy(fontWeight = FontWeight.Bold),
                )
            }
            item(key = "CredentialManagerStepsSection") {
                NumeratedTextList(
                    steps = arrayOf(
                        stringResource(R.string.credential_manager_step_1),
                        stringResource(R.string.credential_manager_step_2),
                        stringResource(R.string.credential_manager_step_3),
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                )
            }
            item(key = "CredentialManagerChip") {
                Box(modifier = Modifier.padding(top = 20.dp)) {
                    WearChip(
                        label = stringResource(R.string.credential_manager_button),
                        onClick = {
                            onSetupStarted()
                            onSetupSuccess()
                        },
                        emphasis = Emphasis.High,
                    )
                }
            }
        }
    }
}

@Composable
@WearPreviewSmallRound
private fun WearSetupCredentialManagerScreenPreview() {
    DashlaneWearPreview {
        WearSetupCredentialManagerContent(
            onSetupStarted = {},
            onSetupSuccess = {},
        )
    }
}