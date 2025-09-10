package com.dashlane.vpn.thirdparty.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashlane.design.component.IndeterminateLoader
import com.dashlane.design.component.ModalBottomSheet
import com.dashlane.help.HelpCenterLink
import com.dashlane.vpn.thirdparty.VpnThirdPartyData
import com.dashlane.vpn.thirdparty.VpnThirdPartyEvent
import com.dashlane.vpn.thirdparty.VpnThirdPartyEvent.ViewResumed
import com.dashlane.vpn.thirdparty.VpnThirdPartyState
import com.dashlane.vpn.thirdparty.VpnThirdPartyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun VpnThirdPartyScreen(
    viewModel: VpnThirdPartyViewModel,
    goToActivateThirdPartyVpn: (String?, List<String>) -> Unit,
    installHotspotShieldApp: (Intent) -> Unit,
    launchHotspotShieldApp: (Intent) -> Unit,
    goToHelpCenterPage: (HelpCenterLink) -> Unit,
) {
    val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)
    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.sendEvent(ViewResumed)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.stateFlow.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is VpnThirdPartyState.SideEffect.GoToSetupVpn -> {
                    goToActivateThirdPartyVpn(sideEffect.defaultEmail, sideEffect.userListSuggestion)
                }

                is VpnThirdPartyState.SideEffect.DownloadApp -> {
                    installHotspotShieldApp(sideEffect.intent)
                }

                is VpnThirdPartyState.SideEffect.LaunchApp -> {
                    launchHotspotShieldApp(sideEffect.intent)
                }

                VpnThirdPartyState.SideEffect.GoToVpnIntroArticle -> {
                    goToHelpCenterPage(HelpCenterLink.ARTICLE_THIRD_PARTY_VPN)
                }

                VpnThirdPartyState.SideEffect.GoToVpnFaqArticle -> {
                    goToHelpCenterPage(HelpCenterLink.ARTICLE_THIRD_PARTY_VPN_FAQ)
                }
            }
        }
    }

    val uiState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()
    when (uiState) {
        is VpnThirdPartyState.View.Initial,
        is VpnThirdPartyState.View.Loading,
        -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                IndeterminateLoader()
            }
        }

        is VpnThirdPartyState.View.SetupVpn,
        is VpnThirdPartyState.View.Activate,
        -> {
            VpnActivateContent(
                onSetupVpnClick = { viewModel.sendEvent(VpnThirdPartyEvent.SetupVpnClick) },
            )
        }

        is VpnThirdPartyState.View.CopyLogin,
        is VpnThirdPartyState.View.CopyPassword,
        is VpnThirdPartyState.View.DownloadApp,
        is VpnThirdPartyState.View.LaunchApp,
        is VpnThirdPartyState.View.Account,
        -> {
            VpnAccountContent(
                login = uiState.data.accountLogin ?: "",
                password = uiState.data.accountPassword ?: "",
                actionType = uiState.data.actionType ?: VpnThirdPartyData.ActionType.DOWNLOAD_APP,
                onLoginCopyClick = { viewModel.sendEvent(VpnThirdPartyEvent.CopyLoginClick) },
                onPasswordCopyClick = { viewModel.sendEvent(VpnThirdPartyEvent.CopyPasswordClick) },
                onShowFaqClick = { viewModel.sendEvent(VpnThirdPartyEvent.ShowFaq) },
                onDownloadHotspotShieldClick = { viewModel.sendEvent(VpnThirdPartyEvent.DownloadAppClick) },
                onLaunchHotspotShieldClick = { viewModel.sendEvent(VpnThirdPartyEvent.LaunchAppClick) },
            )
        }
    }

    if (uiState.data.showFaq) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.sendEvent(VpnThirdPartyEvent.DismissFaq) },
        ) {
            VpnFaqBottomSheetContent(
                onVpnIntroHelpCenterArticleClick = { viewModel.sendEvent(VpnThirdPartyEvent.VpnIntroArticleClick) },
                onVpnFaqHelpCenterArticleClick = { viewModel.sendEvent(VpnThirdPartyEvent.VpnFaqArticleClick) },
            )
        }
    }
}