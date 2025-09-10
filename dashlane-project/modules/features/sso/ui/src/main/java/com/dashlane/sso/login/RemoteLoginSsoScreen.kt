package com.dashlane.sso.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.authentication.sso.GetUserSsoInfoActivity
import com.dashlane.design.component.BottomSheetActions
import com.dashlane.design.component.BottomSheetContent
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.design.component.Text
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.sso.ui.R
import com.dashlane.ui.common.compose.components.TermsAndConditionsDescription
import com.dashlane.ui.widgets.compose.DashlaneLoading
import com.dashlane.util.launchUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteLoginSsoScreen(
    modifier: Modifier = Modifier,
    viewModel: RemoteLoginSsoViewModel,
    cancel: () -> Unit,
    goToMigrationToMasterPasswordUser: (String, Boolean, Boolean) -> Unit,
    goToHome: () -> Unit,
    goToInitialSync: () -> Unit,
    goToOnboardingLock: () -> Unit,
    finishWithResult: (RemoteLoginSsoActivity.Result) -> Unit,
) {
    val context = LocalContext.current
    val startSsoLogin = rememberLauncherForActivityResult(LoginSsoContract(), onResult = viewModel::ssoComplete)

    LaunchedEffect(viewModel) {
        viewModel.viewStarted()
        viewModel.stateFlow.sideEffect.collect { state ->
            when (state) {
                is RemoteLoginSsoState.SideEffect.GoToMigrationToMasterPasswordUser -> goToMigrationToMasterPasswordUser(
                    state.authTicket,
                    state.shouldLaunchInitialSync,
                    state.hasPinOrBiometric,
                )
                is RemoteLoginSsoState.SideEffect.StartSso -> startSsoLogin.launch(state.loginSsoInfo)
                RemoteLoginSsoState.SideEffect.Cancel -> cancel()
                is RemoteLoginSsoState.SideEffect.FinishWithResult -> finishWithResult(state.result)
                RemoteLoginSsoState.SideEffect.GoToHome -> goToHome()
                RemoteLoginSsoState.SideEffect.GoToInitialSync -> goToInitialSync()
                RemoteLoginSsoState.SideEffect.GoToOnboardingLock -> goToOnboardingLock()
                is RemoteLoginSsoState.SideEffect.OpenHelp -> context.launchUrl(state.uri)
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

    RemoteLoginSsoContent(
        modifier = modifier,
        email = uiState.loginSsoInfo?.email ?: "",
    )

    if (uiState.termsOfServiceShown) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        LaunchedEffect(sheetState.targetValue) {
            if (sheetState.currentValue != SheetValue.Hidden && sheetState.targetValue == SheetValue.Hidden) {
                viewModel.bottomSheetDismissed()
            }
        }

        RemoteLoginSsoBottomSheet(
            termsOfServicesAccepted = uiState.termsOfServicesAccepted,
            offersAccepted = uiState.privacyPolicyAccepted,
            sheetState = sheetState,
            bottomSheetDismissed = viewModel::bottomSheetDismissed,
            onCreateAccount = viewModel::createAccount,
            onTosCheckChange = viewModel::tosCheckedChanged,
            onOffersCheckChange = viewModel::offersCheckedChanged,
            onOpenHelpCenterPage = viewModel::onHelpClicked,
        )
    }
}

@Composable
fun RemoteLoginSsoContent(
    modifier: Modifier = Modifier,
    email: String,
) {
    Column(
        modifier = modifier
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DashlaneLogoLockup(height = 40.dp)
        Text(
            text = email,
            style = DashlaneTheme.typography.bodyStandardRegular,
            color = DashlaneTheme.colors.textNeutralStandard,
            modifier = Modifier
                .padding(top = 24.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        DashlaneLoading(
            modifier = Modifier.fillMaxWidth(),
            hasFinishedLoading = false,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        Text(
            text = stringResource(R.string.sso_complete_login_message),
            style = DashlaneTheme.typography.titleSectionMedium,
            color = DashlaneTheme.colors.textNeutralStandard,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteLoginSsoBottomSheet(
    modifier: Modifier = Modifier,
    termsOfServicesAccepted: Boolean,
    offersAccepted: Boolean,
    sheetState: SheetState,
    bottomSheetDismissed: () -> Unit,
    onCreateAccount: () -> Unit,
    onTosCheckChange: (Boolean) -> Unit,
    onOffersCheckChange: (Boolean) -> Unit,
    onOpenHelpCenterPage: (Uri) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = bottomSheetDismissed,
        sheetState = sheetState,
    ) {
        BottomSheetContent(
            modifier = modifier,
            title = stringResource(id = R.string.passwordless_terms_and_conditions_title),
            description = null,
            extraContent = {
                TermsAndConditionsDescription(
                    offersCheck = offersAccepted,
                    onOffersCheckChange = onOffersCheckChange,
                    tosCheck = termsOfServicesAccepted,
                    onTosCheckChange = onTosCheckChange,
                    onOpenHelpCenterPage = onOpenHelpCenterPage,
                )
            },
            actions = BottomSheetActions(
                primaryButtonText = stringResource(id = R.string.passwordless_account_creation_create_account_button),
                isPrimaryButtonEnabled = termsOfServicesAccepted,
                onPrimaryButtonClick = onCreateAccount,
                secondaryButtonText = null,
                onSecondaryButtonClick = null,
            ),
        )
    }
}

@Preview
@Composable
private fun RemoteLoginSsoContentPreview() {
    DashlanePreview {
        RemoteLoginSsoContent(
            email = "randomemail@provider.com",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun RemoteLoginSsoBottomSheetPreview() {
    DashlanePreview {
        RemoteLoginSsoBottomSheet(
            termsOfServicesAccepted = true,
            offersAccepted = true,
            sheetState = SheetState(skipPartiallyExpanded = true, density = Density(1.0f, 1.0f), initialValue = SheetValue.Expanded),
            bottomSheetDismissed = {},
            onCreateAccount = {},
            onTosCheckChange = {},
            onOffersCheckChange = {},
            onOpenHelpCenterPage = {},
        )
    }
}

class LoginSsoContract : ActivityResultContract<LoginSsoInfo, GetSsoInfoResult?>() {
    override fun createIntent(context: Context, input: LoginSsoInfo): Intent =
        GetUserSsoInfoActivity.createStartIntent(context, input.email, input.serviceProviderUrl, input.isNitroProvider)

    override fun parseResult(resultCode: Int, intent: Intent?): GetSsoInfoResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.let { IntentCompat.getParcelableExtra(intent, RemoteLoginSsoActivity.KEY_RESULT, GetSsoInfoResult::class.java) }
    }
}