package com.dashlane.sso.migration

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.DashlaneLogoLockup
import com.dashlane.design.component.ExpressiveIcon
import com.dashlane.design.component.ExpressiveIconSize
import com.dashlane.design.component.Text
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.help.HelpCenterLink
import com.dashlane.sso.login.LoginSsoContract
import com.dashlane.sso.ui.R
import com.dashlane.ui.activities.intro.DescriptionItem
import com.dashlane.ui.activities.intro.IntroScreen
import com.dashlane.ui.activities.intro.LinkItem
import com.dashlane.ui.widgets.compose.DashlaneLoading
import com.dashlane.util.launchUrl

@Composable
fun MigrationToSsoScreen(
    modifier: Modifier = Modifier,
    viewModel: MigrationToSsoViewModel,
    snackbarHostState: SnackbarHostState,
    finish: (Boolean, Boolean) -> Unit,
    cancel: () -> Unit,
) {
    BackHandler {
        viewModel.sendEvent(MigrationToSsoEvent.Cancel)
    }

    val startSsoLogin = rememberLauncherForActivityResult(
        contract = LoginSsoContract(),
        onResult = { result -> viewModel.sendEvent(MigrationToSsoEvent.SsoCompleted(result = result)) },
    )
    val context = LocalContext.current

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                MigrationToSsoState.SideEffect.Cancel -> cancel()
                is MigrationToSsoState.SideEffect.GoToSso -> startSsoLogin.launch(sideEffect.loginSsoInfo)
                is MigrationToSsoState.SideEffect.Finish -> finish(sideEffect.shouldLaunchInitialSync, sideEffect.goToApplicationLock)
                is MigrationToSsoState.SideEffect.OpenLink -> context.launchUrl(sideEffect.url)
                is MigrationToSsoState.SideEffect.ShowError -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.sso_error_not_correct))
                }
            }
        }
    }

    if (uiState.data.isLoading) {
        MigrationToSsoLoadingContent()
    } else {
        MigrationToSsoContent(
            modifier = modifier,
            onCancel = { viewModel.sendEvent(MigrationToSsoEvent.Cancel) },
            onClickPositiveButton = { viewModel.sendEvent(MigrationToSsoEvent.Continue) },
            onClickExternalLink = { url -> viewModel.sendEvent(MigrationToSsoEvent.LinkClicked(url)) },
        )
    }
}

@Composable
fun MigrationToSsoContent(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onClickPositiveButton: () -> Unit,
    onClickExternalLink: (String) -> Unit,
) {
    IntroScreen(
        modifier = modifier,
        titleResId = R.string.sso_member_migration_intro_title,
        descriptionItems = listOf(
            DescriptionItem(
                imageIconToken = null,
                titleResId = R.string.sso_member_migration_intro_description,
            ),
        ),
        linkItems = listOf(
            LinkItem.ExternalLinkItem(
                linkResId = R.string.sso_member_migration_intro_link,
                link = HelpCenterLink.ARTICLE_SSO_LOGIN.uri.toString(),
            ),
        ),
        onClickLink = {
            if (it is LinkItem.ExternalLinkItem) {
                onClickExternalLink(it.link)
            }
        },
        positiveButtonResId = R.string.sso_member_migration_intro_cta_positive,
        negativeButtonResId = R.string.sso_member_migration_intro_cta_negative,
        onClickNegativeButton = onCancel,
        onClickPositiveButton = onClickPositiveButton,
        illustration = {
            Box(modifier = Modifier.fillMaxSize()) {
                ExpressiveIcon(
                    modifier = Modifier.align(Alignment.BottomStart),
                    icon = IconTokens.ssoOutlined,
                    size = ExpressiveIconSize.Large,
                )
            }
        },
    )
}

@Composable
fun MigrationToSsoLoadingContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(bottom = 18.dp, top = 24.dp, start = 24.dp, end = 24.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DashlaneLogoLockup(height = 40.dp)
        Spacer(modifier = Modifier.weight(1f))
        DashlaneLoading(
            modifier = Modifier.fillMaxWidth(),
            hasFinishedLoading = false,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        Text(
            text = stringResource(R.string.sso_member_migration_message),
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

@Preview
@Composable
private fun MigrationToSsoContentPreview() = DashlanePreview {
    MigrationToSsoContent(
        onCancel = {},
        onClickPositiveButton = {},
        onClickExternalLink = {},
    )
}

@Preview
@Composable
private fun MigrationToSsoLoadingContentPreview() {
    DashlanePreview { MigrationToSsoLoadingContent() }
}