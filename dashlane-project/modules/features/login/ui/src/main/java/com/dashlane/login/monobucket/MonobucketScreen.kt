package com.dashlane.login.monobucket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.data.login.Device
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.Text
import com.dashlane.design.component.tooling.DashlaneModalBottomSheetPreview
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.devicelimit.DeviceContent
import com.dashlane.login.ui.R
import com.dashlane.ui.common.compose.components.WarningContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonobucketScreen(
    modifier: Modifier = Modifier,
    viewModel: MonobucketViewModel,
    goToPremium: () -> Unit,
    hasSync: () -> Unit,
    confirmUnregisterDevice: () -> Unit,
    logout: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                MonobucketState.SideEffect.Premium -> goToPremium()
                MonobucketState.SideEffect.ConfirmUnregisterDevice -> confirmUnregisterDevice()
                MonobucketState.SideEffect.UserLoggedOut -> logout()
                MonobucketState.SideEffect.HasSync -> hasSync()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    MonobucketContent(
        modifier = modifier,
        onLogout = viewModel::onLogOut,
        onPrimaryButtonClick = viewModel::onUpgradePremium,
        onSecondaryButtonClick = viewModel::unlinkPreviousDevice,
    )

    if (uiState.showPreviousDeviceDialog) {
        uiState.device?.let { device ->
            val sheetState = rememberModalBottomSheetState(
                confirmValueChange = { sheetValue ->
                    if (sheetValue == SheetValue.Hidden) viewModel.bottomSheetDismissed()
                    true
                },
            )

            ModalBottomSheet(
                onDismissRequest = viewModel::bottomSheetDismissed,
                sheetState = sheetState,
            ) {
                MonobucketDeviceContent(
                    device = device,
                    unlink = viewModel::onConfirmUnregisterDevice,
                    cancel = viewModel::bottomSheetDismissed,
                )
            }
        }
    }
}

@Composable
private fun MonobucketContent(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp, top = 8.dp),
            onClick = onLogout,
            layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.login_monobucket_log_out)),
            intensity = Intensity.Supershy,
            mood = Mood.Brand,
        )
        WarningContent(
            painter = painterResource(id = R.drawable.ic_sync_multi_device),
            contentDescription = stringResource(id = R.string.and_accessibility_content_desc_sync_multi_device_logo),
            title = stringResource(id = R.string.login_monobucket_title),
            description = stringResource(id = R.string.login_monobucket_subtitle),
            infoBox = stringResource(id = R.string.login_device_limit_tip),
            primaryButtonText = stringResource(id = R.string.login_monobucket_see_premium_plan),
            secondaryButtonText = stringResource(id = R.string.login_monobucket_unlink_previous_device),
            onPrimaryButtonClick = onPrimaryButtonClick,
            onSecondaryButtonClick = onSecondaryButtonClick,
        )
    }
}

@Composable
fun MonobucketDeviceContent(
    modifier: Modifier = Modifier,
    device: Device,
    unlink: () -> Unit,
    cancel: () -> Unit,
) {
    BottomSheetContent(
        modifier = modifier,
        title = stringResource(id = R.string.login_monobucket_unregister_device_title),
        description = null,
        extraContent = {
            DeviceContent(
                modifier = Modifier
                    .background(
                        color = DashlaneTheme.colors.containerAgnosticNeutralQuiet,
                        shape = RoundedCornerShape(size = 8.dp),
                    )
                    .padding(all = 8.dp),
                device = device,
                selectDevice = null,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.login_monobucket_unregister_device_message),
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
            )
        },
        actions = BottomSheetActions(
            primaryButtonText = stringResource(id = R.string.login_monobucket_unregister_device_unlink),
            onPrimaryButtonClick = unlink,
            secondaryButtonText = stringResource(id = R.string.login_monobucket_unregister_device_cancel),
            onSecondaryButtonClick = cancel,
        ),
    )
}

@Preview
@Composable
private fun MonobucketContentPreview() {
    DashlanePreview {
        MonobucketContent(
            onLogout = {},
            onPrimaryButtonClick = {},
            onSecondaryButtonClick = {},
        )
    }
}

@Preview
@Composable
private fun MonobucketDeviceContentPreview() {
    DashlaneModalBottomSheetPreview {
        MonobucketDeviceContent(
            device = Device(
                id = "id",
                name = "Rob's Macbook Pro",
                iconResId = R.drawable.device_apple,
                lastActivityDate = System.currentTimeMillis(),
            ),
            unlink = { },
            cancel = { },
        )
    }
}