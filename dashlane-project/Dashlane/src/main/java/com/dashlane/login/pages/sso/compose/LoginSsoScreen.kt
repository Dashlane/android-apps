package com.dashlane.login.pages.sso.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.R
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.authentication.sso.GetUserSsoInfoActivity
import com.dashlane.design.component.ButtonLayout
import com.dashlane.design.component.ButtonMedium
import com.dashlane.design.component.ButtonMediumBar
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.lock.LockSetting
import com.dashlane.login.pages.password.compose.LoginPasswordSwitchAccount
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getBaseActivity
import com.dashlane.util.getParcelableExtraCompat

@Composable
fun LoginSsoScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginSsoViewModel,
    userAccountInfo: UserAccountInfo,
    lockSetting: LockSetting,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    changeAccount: (String?) -> Unit
) {
    val goToSso = rememberLauncherForActivityResult(GetSsoInfoContract(), onResult = viewModel::ssoComplete)

    LaunchedEffect(viewModel) {
        viewModel.viewStarted(userAccountInfo, lockSetting)
        viewModel.navigationState.collect { state ->
            when (state) {
                is LoginSsoNavigationState.GoToSso -> goToSso.launch(state.ssoInfo)
                LoginSsoNavigationState.Cancel -> onCancel()
                is LoginSsoNavigationState.ChangeAccount -> changeAccount(state.email)
                LoginSsoNavigationState.UnlockSuccess -> onSuccess()
            }
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    uiState.error?.let { error ->
        context.getBaseActivity()?.let { activity -> SnackbarUtils.showSnackbar(activity, error.toErrorMessage()) }
    }

    LoginSsoContent(
        modifier = modifier,
        email = userAccountInfo.username,
        loginHistory = uiState.loginHistory,
        isLoading = uiState.isLoading,
        isCancellable = uiState.lockSetting?.isLockCancelable == true,
        onNext = viewModel::onNextClicked,
        onCancel = viewModel::onCancelClicked,
        changeAccount = viewModel::changeAccount
    )
}

@Composable
@VisibleForTesting
fun LoginSsoContent(
    modifier: Modifier = Modifier,
    email: String,
    loginHistory: List<String>,
    isLoading: Boolean,
    isCancellable: Boolean,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    changeAccount: (String?) -> Unit,
) {
    Column(
        modifier = modifier
            .heightIn(min = 300.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        DashlaneLogoLockup(height = 40.dp)
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = stringResource(id = R.string.sso_lock_topic_default),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralCatchy,
        )
        if (loginHistory.isNotEmpty()) {
            LoginPasswordSwitchAccount(email = email, loginHistory = loginHistory, changeAccount)
        } else {
            Text(
                text = email,
                style = DashlaneTheme.typography.bodyStandardRegular,
                color = DashlaneTheme.colors.textNeutralStandard,
                modifier = Modifier
                    .padding(top = 24.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isCancellable) {
            ButtonMediumBar(
                primaryButtonLayout = if (isLoading) ButtonLayout.IndeterminateProgress else ButtonLayout.TextOnly(text = stringResource(id = R.string.login_password_page_login_button)),
                onPrimaryButtonClick = onNext,
                secondaryButtonLayout = ButtonLayout.TextOnly(text = stringResource(id = R.string.cancel)),
                onSecondaryButtonClick = onCancel
            )
        } else {
            ButtonMedium(
                modifier = Modifier
                    .align(Alignment.End)
                    .widthIn(min = 80.dp),
                onClick = onNext,
                intensity = Intensity.Catchy,
                layout = if (isLoading) ButtonLayout.IndeterminateProgress else ButtonLayout.TextOnly(text = stringResource(id = R.string.login_password_page_login_button))
            )
        }
    }
}

@Composable
private fun LoginSsoError.toErrorMessage(): String {
    return when (this) {
        LoginSsoError.Offline -> stringResource(id = R.string.offline)
        LoginSsoError.Generic -> stringResource(id = R.string.error)
        LoginSsoError.InvalidSso -> stringResource(id = R.string.sso_error_not_correct)
        LoginSsoError.Network -> stringResource(id = R.string.network_error)
    }
}

@Preview
@Composable
private fun LoginSSOPreview() {
    DashlanePreview {
        LoginSsoContent(
            email = "randomemail@provider.com",
            loginHistory = emptyList(),
            isLoading = true,
            isCancellable = true,
            onNext = { },
            onCancel = { },
            changeAccount = { }
        )
    }
}

class GetSsoInfoContract : ActivityResultContract<SsoInfo, GetSsoInfoResult>() {
    override fun createIntent(context: Context, input: SsoInfo): Intent {
        return GetUserSsoInfoActivity.createStartIntent(
            context = context,
            login = input.login,
            serviceProviderUrl = input.serviceProviderUrl,
            isNitroProvider = input.isNitroProvider
        )
    }

    override fun parseResult(resultCode: Int, intent: Intent?): GetSsoInfoResult {
        if (resultCode == Activity.RESULT_CANCELED) return GetSsoInfoResult.Error.Unknown

        return intent?.getParcelableExtraCompat<GetSsoInfoResult>(GetUserSsoInfoActivity.KEY_RESULT)
            ?: GetSsoInfoResult.Error.Unknown
    }
}
