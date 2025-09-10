package com.dashlane.login.devicelimit

import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.ListItem
import com.dashlane.design.component.Text
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.component.cardBackground
import com.dashlane.design.iconography.IconToken
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.DecorativeColor
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.login.ui.R
import com.dashlane.ui.common.compose.components.WarningContent

@Composable
fun DeviceLimitScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceLimitViewModel,
    goToPremium: () -> Unit,
    hasSync: () -> Unit,
    confirmUnregisterDevice: (List<Device>) -> Unit,
    logout: () -> Unit,
) {
    BackHandler {
        viewModel.onBackPressed()
    }

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                is DeviceLimitState.SideEffect.ConfirmUnregisterDevice -> confirmUnregisterDevice(state.devices)
                DeviceLimitState.SideEffect.HasSync -> hasSync()
                DeviceLimitState.SideEffect.Premium -> goToPremium()
                DeviceLimitState.SideEffect.UserLoggedOut -> logout()
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    if (uiState.showDevicesDialog) {
        DeviceListContent(
            modifier = modifier,
            maxDevices = uiState.maxDevices,
            devices = uiState.devices,
            unlinkEnabled = uiState.unlinkEnabled,
            selectDevice = viewModel::deviceSelected,
            unlink = viewModel::onConfirmUnregisterDevice,
            cancel = viewModel::cancel,
        )
    } else {
        DeviceLimitContent(
            modifier = modifier,
            maxDevices = uiState.maxDevices,
            onLogout = viewModel::onLogOut,
            onPrimaryButtonClick = viewModel::onUpgradePremium,
            onSecondaryButtonClick = viewModel::unlinkDevices,
        )
    }
}

@Composable
private fun DeviceLimitContent(
    modifier: Modifier = Modifier,
    maxDevices: Int,
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
            layout = ButtonLayout.TextOnly(text = stringResource(id = R.string.login_device_limit_log_out)),
            intensity = Intensity.Supershy,
            mood = Mood.Brand,
        )
        WarningContent(
            painter = painterResource(id = R.drawable.ic_sync_multi_device),
            contentDescription = stringResource(id = R.string.and_accessibility_content_desc_sync_multi_device_logo),
            title = stringResource(R.string.login_device_limit_title_dynamic, maxDevices),
            description = stringResource(R.string.login_device_limit_subtitle),
            infoBox = stringResource(id = R.string.login_device_limit_tip),
            primaryButtonText = stringResource(id = R.string.login_device_limit_see_premium_plan),
            secondaryButtonText = stringResource(R.string.login_device_limit_unlink_previous_device),
            onPrimaryButtonClick = onPrimaryButtonClick,
            onSecondaryButtonClick = onSecondaryButtonClick,
        )
    }
}

@Suppress("LongMethod")
@Composable
fun DeviceListContent(
    modifier: Modifier = Modifier,
    maxDevices: Int,
    devices: List<Device>,
    unlinkEnabled: Boolean,
    selectDevice: (Device, Boolean) -> Unit,
    unlink: () -> Unit,
    cancel: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.login_device_limit_unregister_devices_title),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
        )
        Text(
            text = stringResource(id = R.string.login_device_limit_unregister_devices_message_dynamic, maxDevices),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(bottom = 16.dp),
        )
        Text(
            text = stringResource(id = R.string.login_device_limit_unregister_devices_caption),
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralQuiet,
            modifier = Modifier
                .padding(bottom = 16.dp),
        )
        Box(
            modifier = Modifier.weight(weight = 1f, fill = true),
        ) {
            Column(
                modifier = Modifier
                    .cardBackground()
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
            ) {
                devices.forEach { device ->
                    DeviceContent(
                        device = device,
                        selectDevice = selectDevice,
                    )
                }
            }
        }
        ButtonMediumBar(
            modifier = Modifier.padding(top = 16.dp),
            primaryText = stringResource(id = R.string.login_monobucket_unregister_device_unlink),
            secondaryText = stringResource(id = R.string.login_monobucket_unregister_device_cancel),
            onPrimaryButtonClick = unlink,
            onSecondaryButtonClick = cancel,
            isPrimaryButtonEnabled = unlinkEnabled,
        )
    }
}

@Composable
fun DeviceContent(
    modifier: Modifier = Modifier,
    device: Device,
    selectDevice: ((Device, Boolean) -> Unit)?,
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth(),
        checked = if (selectDevice != null) device.selected else null,
        thumbnailType = ThumbnailType.VaultItem.OtherIcon(
            token = IconToken(device.iconResId),
            color = DecorativeColor.GREY,
        ),
        title = device.name,
        description = stringResource(
            id = R.string.login_device_limit_unregister_device_date,
            DateUtils.getRelativeTimeSpanString(device.lastActivityDate),
        ),
        onClick = selectDevice?.let {
            { selectDevice(device, !device.selected) }
        },
    )
}

@Preview
@Composable
private fun DevicesContentPreview() {
    DashlanePreview {
        DeviceLimitContent(
            maxDevices = 2,
            onLogout = { },
            onPrimaryButtonClick = { },
            onSecondaryButtonClick = { },
        )
    }
}

@Preview
@Composable
private fun DeviceListPreview() {
    DashlanePreview {
        DeviceListContent(
            maxDevices = 2,
            devices = listOf(
                Device(
                    id = "id",
                    name = "Rob's Macbook Pro with a very long name",
                    iconResId = R.drawable.device_apple,
                    lastActivityDate = System.currentTimeMillis(),
                ),
                Device(
                    id = "id",
                    name = "Rob's Macbook Pro",
                    iconResId = R.drawable.device_apple,
                    lastActivityDate = System.currentTimeMillis(),
                    selected = true,
                ),
            ),
            unlinkEnabled = false,
            selectDevice = { _, _ -> },
            unlink = { },
            cancel = { },
        )
    }
}

@Preview
@Composable
private fun DevicePreview() {
    var selected = false
    DashlanePreview {
        DeviceContent(
            device = Device(
                id = "id",
                name = "Rob's Macbook Pro",
                iconResId = R.drawable.device_apple,
                lastActivityDate = System.currentTimeMillis(),
                selected = selected,
            ),
            selectDevice = { _, _ -> selected = !selected },
        )
    }
}