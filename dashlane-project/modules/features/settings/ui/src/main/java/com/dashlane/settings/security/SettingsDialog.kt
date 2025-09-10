package com.dashlane.settings.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.BaseDialog
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.Dialog
import com.dashlane.design.component.Radio
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.settings.security.SettingsDialog.AutoLockTimeSelector
import com.dashlane.settings.ui.R

sealed interface SettingsDialog {
    data class WeakBiometricWarning(val hasPin: Boolean) : SettingsDialog

    data object WeakBiometricRecoveryWarning : SettingsDialog

    data object BiometricRecoveryAutoDisable : SettingsDialog

    data object DisableBiometricRecovery : SettingsDialog

    data object Disable2FA : SettingsDialog

    data class AutoLockTimeSelector(val selected: Int) : SettingsDialog

    data class KeyDerivationWarning(val hasCryptoForcedPayload: Boolean) : SettingsDialog

    data object Logout : SettingsDialog

    data object LogoutPasswordless : SettingsDialog
}

@Suppress("LongMethod")
@Composable
fun SettingsDialog(
    dialog: SettingsDialog,
    onConfirm: (SettingsDialog) -> Unit,
    onDismiss: (SettingsDialog) -> Unit,
) {
    when (dialog) {
        is AutoLockTimeSelector -> {
            AutoLockTimePickerDialog(
                selected = dialog.selected,
                onConfirm = { index -> onConfirm(AutoLockTimeSelector(index)) },
                onDismiss = { onDismiss(dialog) },
            )
        }
        SettingsDialog.BiometricRecoveryAutoDisable -> {
            DisableBiometricRecoveryAutoDialog(
                onConfirm = { onConfirm(dialog) },
                onDismiss = { onDismiss(dialog) },
            )
        }
        SettingsDialog.Disable2FA -> {
            Disable2FADialog(
                onConfirm = { onConfirm(dialog) },
                onDismiss = { onDismiss(dialog) },
            )
        }
        SettingsDialog.DisableBiometricRecovery -> {
            DisableBiometricRecoveryDialog(
                onConfirm = { onConfirm(dialog) },
                onDismiss = { onDismiss(dialog) },
            )
        }
        is SettingsDialog.KeyDerivationWarning -> {
            KeyDerivationDialog(
                hasCryptoForcedPayload = dialog.hasCryptoForcedPayload,
                onDismiss = { onDismiss(dialog) },
            )
        }
        SettingsDialog.Logout -> {
            LogoutDialog(
                onConfirm = { onConfirm(dialog) },
                onDismiss = { onDismiss(dialog) },
            )
        }
        SettingsDialog.LogoutPasswordless -> {
            LogoutPasswordlessDialog(
                onConfirm = { onConfirm(dialog) },
                onDismiss = { onDismiss(dialog) },
            )
        }
        SettingsDialog.WeakBiometricRecoveryWarning -> {
            WeakBiometricRecoveryWarningDialog(
                onConfirm = { onConfirm(dialog) },
                onDismiss = { onDismiss(dialog) },
            )
        }
        is SettingsDialog.WeakBiometricWarning -> {
            WeakBiometricWarningDialog(
                hasPin = dialog.hasPin,
                onConfirm = { onConfirm(dialog) },
                onDismiss = { onDismiss(dialog) },
            )
        }
    }
}

@Composable
fun WeakBiometricWarningDialog(
    hasPin: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val positiveButtonText = if (hasPin) {
        R.string.biometric_settings_weak_warning_dialog_positive_button_keep
    } else {
        R.string.biometric_settings_weak_warning_dialog_positive_button_setup
    }
    Dialog(
        title = stringResource(id = R.string.biometric_settings_weak_warning_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.biometric_settings_weak_warning_dialog_message))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(positiveButtonText)),
        mainActionClick = onConfirm,
        additionalActionLayout = ButtonLayout.TextOnly(
            stringResource(id = R.string.biometric_settings_weak_warning_dialog_negative_button),
        ),
        additionalActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun WeakBiometricRecoveryWarningDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.account_recovery_weak_warning_dialog_title),
        description = {
            Text(text = stringResource(id = R.string.account_recovery_weak_warning_dialog_message))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(R.string.account_recovery_weak_warning_dialog_positive_button)),
        mainActionClick = onConfirm,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cancel)),
        additionalActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun DisableBiometricRecoveryDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.account_recovery_deactivation_title),
        description = {
            Text(text = stringResource(id = R.string.account_recovery_deactivation_message))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(R.string.account_recovery_deactivation_positive_cta)),
        mainActionClick = onConfirm,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.account_recovery_deactivation_negative_cta)),
        additionalActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun DisableBiometricRecoveryAutoDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.account_recovery_biometric_deactivation_title),
        description = {
            Text(text = stringResource(id = R.string.account_recovery_biometric_deactivation_message))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(R.string.account_recovery_biometric_deactivation_positive_cta)),
        mainActionClick = onConfirm,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cancel)),
        additionalActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun Disable2FADialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.disable_totp_confirmation_title),
        description = {
            Text(text = stringResource(id = R.string.disable_totp_confirmation_message))
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(R.string.disable_totp_confirmation_cta_positive)),
        mainActionClick = onConfirm,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(id = R.string.cancel)),
        additionalActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun AutoLockTimePickerDialog(
    selected: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    BaseDialog(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .selectableGroup(),
        ) {
            Text(
                text = stringResource(R.string.setting_choose_timeout),
                style = DashlaneTheme.typography.bodyStandardRegular,
            )
            stringArrayResource(R.array.timeout_strings)
                .forEachIndexed { index, text ->
                    Radio(
                        selected = index == selected,
                        onClick = { onConfirm(index) },
                    ) {
                        Text(text)
                    }
                }
        }
    }
}

@Composable
fun KeyDerivationDialog(
    hasCryptoForcedPayload: Boolean,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.settings_cryptography_dialog_title),
        description = {
            if (hasCryptoForcedPayload) {
                Text(text = stringResource(id = R.string.settings_cryptography_dialog_description_b2b))
            } else {
                Text(text = stringResource(id = R.string.settings_cryptography_dialog_description_b2c))
            }
        },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(R.string.ok)),
        mainActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.logout),
        description = { Text(text = stringResource(id = R.string.log_out_of_dashlane_)) },
        mainActionLayout = ButtonLayout.TextOnly(stringResource(R.string.yes)),
        mainActionClick = onConfirm,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(R.string.no)),
        additionalActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun LogoutPasswordlessDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(id = R.string.log_out_dialog_title),
        description = { Text(text = stringResource(id = R.string.log_out_dialog_description)) },
        isDestructive = true,
        mainActionLayout = ButtonLayout.TextOnly(stringResource(R.string.log_out_dialog_positive_button)),
        mainActionClick = onConfirm,
        additionalActionLayout = ButtonLayout.TextOnly(stringResource(R.string.log_out_dialog_negative_button)),
        additionalActionClick = onDismiss,
        onDismissRequest = onDismiss,
    )
}

@Preview
@Composable
private fun WeakBiometricWarningDialogPreview() {
    DashlanePreview {
        WeakBiometricWarningDialog(
            hasPin = true,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun WeakBiometricRecoveryWarningDialogPreview() {
    DashlanePreview {
        WeakBiometricRecoveryWarningDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun DisableBiometricRecoveryDialogPreview() {
    DashlanePreview {
        DisableBiometricRecoveryDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun KeyDerivationDialogPreview() {
    DashlanePreview {
        KeyDerivationDialog(
            hasCryptoForcedPayload = true,
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun AutoLockTimePickerDialogPreview() {
    DashlanePreview {
        AutoLockTimePickerDialog(
            selected = 2,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun Disable2FADialogPreview() {
    DashlanePreview { Disable2FADialog(onConfirm = {}, onDismiss = {}) }
}

@Preview
@Composable
private fun LogoutDialogPreview() {
    DashlanePreview { LogoutDialog(onConfirm = {}, onDismiss = {}) }
}

@Preview
@Composable
private fun LogoutPasswordlessDialogPreview() {
    DashlanePreview { LogoutPasswordlessDialog(onConfirm = {}, onDismiss = {}) }
}