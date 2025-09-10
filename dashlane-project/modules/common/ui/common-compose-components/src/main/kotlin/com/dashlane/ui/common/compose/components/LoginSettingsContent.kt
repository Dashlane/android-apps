package com.dashlane.ui.common.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.DashlaneSnackbarWrapper
import com.dashlane.design.component.HtmlText
import com.dashlane.design.component.LinkButton
import com.dashlane.design.component.LinkButtonDestinationType
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.ui.commoncomposecomponents.R

const val LOGIN_SETTINGS_BIOMETRIC_TOGGLE_TEST_TAG = "loginSettingsBiometricToggleTestTag"
const val LOGIN_SETTINGS_BIOMETRIC_RECOVERY_TOGGLE_TEST_TAG = "loginSettingsBiometricRecoveryToggleTestTag"

@Composable
@Suppress("LongMethod", "kotlin:S107")
fun LoginSettingsContent(
    modifier: Modifier = Modifier,
    helpShown: Boolean,
    snackBarShown: Boolean,
    biometricChecked: Boolean,
    biometricRecoveryChecked: Boolean,
    onBiometricCheckedChange: (Boolean) -> Unit,
    onBiometricRecoveryCheckedChange: (Boolean) -> Unit,
    onHelpButton: () -> Unit,
    onNext: () -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(snackBarShown) {
        if (snackBarShown) snackBarHostState.showSnackbar(context.getString(R.string.create_account_settings_warning_toast))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, start = 24.dp, top = 24.dp, end = 24.dp),
    ) {
        DashlaneLogoLockup(height = 40.dp)
        Text(
            text = stringResource(id = R.string.login_settings_title),
            style = DashlaneTheme.typography.titleSectionLarge,
            color = DashlaneTheme.colors.textNeutralCatchy,
            modifier = Modifier
                .padding(top = 32.dp),
        )
        Column(
            modifier = Modifier
                .padding(top = 32.dp)
                .background(DashlaneTheme.colors.containerAgnosticNeutralSupershy, shape = RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            SettingField(
                modifier = Modifier.testTag(LOGIN_SETTINGS_BIOMETRIC_TOGGLE_TEST_TAG),
                title = stringResource(id = R.string.create_account_settings_biometric),
                description = stringResource(id = R.string.create_account_settings_biometric_desc),
                checked = biometricChecked,
                onCheckedChange = onBiometricCheckedChange,
            )
            SettingField(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .testTag(LOGIN_SETTINGS_BIOMETRIC_RECOVERY_TOGGLE_TEST_TAG),
                title = stringResource(id = R.string.create_account_settings_resetmp),
                description = stringResource(R.string.create_account_settings_resetmp_desc),
                checked = biometricRecoveryChecked,
                onCheckedChange = onBiometricRecoveryCheckedChange,
            )
            LinkButton(
                modifier = Modifier.padding(top = 8.dp),
                onClick = onHelpButton,
                text = stringResource(R.string.create_account_settings_info_cta),
                destinationType = LinkButtonDestinationType.INTERNAL,
            )
            AnimatedVisibility(helpShown) {
                HtmlText(
                    htmlText = stringResource(id = R.string.create_account_settings_info_description),
                    style = DashlaneTheme.typography.bodyHelperRegular,
                    color = DashlaneTheme.colors.textNeutralStandard,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        ButtonMedium(
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.End),
            onClick = onNext,
            mood = Mood.Brand,
            intensity = Intensity.Catchy,
            layout = ButtonLayout.TextOnly(
                text = stringResource(id = R.string.login_settings_complete_cta),
            ),
        )
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        DashlaneSnackbarWrapper {
            SnackbarHost(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                hostState = snackBarHostState,
            )
        }
    }
}

@Preview
@Composable
private fun LoginSettingsContentPreview() {
    DashlanePreview {
        LoginSettingsContent(
            helpShown = true,
            snackBarShown = true,
            biometricChecked = true,
            biometricRecoveryChecked = true,
            onBiometricCheckedChange = {},
            onBiometricRecoveryCheckedChange = {},
            onHelpButton = {},
            onNext = {},
        )
    }
}