package com.dashlane.login.monobucket

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.Device
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
        onSecondaryButtonClick = viewModel::unlinkPreviousDevice
    )

    if (uiState.showPreviousDeviceDialog) {
        uiState.device?.let { device ->
            val sheetState = rememberModalBottomSheetState(
                confirmValueChange = { sheetValue ->
                    if (sheetValue == SheetValue.Hidden) viewModel.bottomSheetDismissed()
                    true
                }
            )

            ModalBottomSheet(
                containerColor = DashlaneTheme.colors.containerAgnosticNeutralSupershy,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                onDismissRequest = viewModel::bottomSheetDismissed,
                sheetState = sheetState
            ) {
                MonobucketDeviceContent(
                    modifier = modifier,
                    device = device,
                    unlink = viewModel::onConfirmUnregisterDevice,
                    cancel = viewModel::bottomSheetDismissed
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
        modifier = modifier.fillMaxSize()
    ) {
        ButtonMedium(
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp, top = 8.dp),
            onClick = onLogout,
            layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.login_monobucket_log_out)),
            intensity = Intensity.Supershy,
            mood = Mood.Brand
        )
        WarningContent(
            modifier = modifier,
            painter = painterResource(id = R.drawable.ic_sync_multi_device),
            contentDescription = stringResource(id = R.string.and_accessibility_content_desc_sync_multi_device_logo),
            title = stringResource(id = R.string.login_monobucket_title),
            description = stringResource(id = R.string.login_monobucket_subtitle),
            infoBox = stringResource(id = R.string.login_device_limit_tip),
            primaryButtonText = stringResource(id = R.string.login_monobucket_see_premium_plan),
            secondaryButtonText = stringResource(id = R.string.login_monobucket_unlink_previous_device),
            onPrimaryButtonClick = onPrimaryButtonClick,
            onSecondaryButtonClick = onSecondaryButtonClick
        )
    }
}

@Suppress("LongMethod")
@Composable
fun MonobucketDeviceContent(
    modifier: Modifier = Modifier,
    device: Device,
    unlink: () -> Unit,
    cancel: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.login_monobucket_unregister_device_title),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.padding(end = 16.dp),
                painter = painterResource(id = device.iconResId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(DashlaneTheme.colors.oddityBrand),
            )
            Column {
                Text(
                    text = device.name,
                    style = DashlaneTheme.typography.bodyStandardRegular,
                    color = DashlaneTheme.colors.textNeutralStandard,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(
                        id = R.string.login_monobucket_unregister_device_date,
                        DateUtils.getRelativeTimeSpanString(device.lastActivityDate)
                    ),
                    style = DashlaneTheme.typography.bodyReducedRegular,
                    color = DashlaneTheme.colors.textNeutralQuiet,
                )
            }
        }

        Text(
            text = stringResource(id = R.string.login_monobucket_unregister_device_message),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
        )

        ButtonMediumBar(
            modifier = Modifier.padding(bottom = 32.dp),
            primaryText = stringResource(id = R.string.login_monobucket_unregister_device_unlink),
            secondaryText = stringResource(id = R.string.login_monobucket_unregister_device_cancel),
            onPrimaryButtonClick = unlink,
            onSecondaryButtonClick = cancel,
        )
    }
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
    DashlanePreview {
        MonobucketDeviceContent(
            device = Device(
                id = "id",
                name = "Rob's Macbook Pro",
                iconResId = R.drawable.device_apple,
                lastActivityDate = System.currentTimeMillis()
            ),
            unlink = { },
            cancel = { }
        )
    }
}