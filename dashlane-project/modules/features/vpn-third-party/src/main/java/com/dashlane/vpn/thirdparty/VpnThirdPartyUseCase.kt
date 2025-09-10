package com.dashlane.vpn.thirdparty

import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.clipboard.ClipboardCopy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun getHotspotShieldAccount(
    repository: VpnThirdPartyRepository,
    preferences: GlobalPreferencesManager,
    state: VpnThirdPartyState.View.Loading,
): Flow<VpnThirdPartyEvent> {
    return flow {
        val account = repository.getHotspotShieldAccount()
        if (account != null) {
            emit(
                VpnThirdPartyEvent.AccountLoaded(
                    login = account.login,
                    password = account.password,
                    itemId = account.itemId,
                    domain = account.domain,
                    isHotspotShieldInstalled = repository.isHotspotShieldInstalled,
                ),
            )
        } else {
            val defaultEmail = preferences.getLastLoggedInUser()
            val userListSuggestion = preferences.getUserListHistory()
            emit(
                VpnThirdPartyEvent.NoAccountLoaded(
                    defaultEmail = defaultEmail,
                    userListSuggestion = userListSuggestion,
                ),
            )
        }
    }
}

fun copyLogin(
    clipboardCopy: ClipboardCopy,
    logger: VpnThirdPartyLogger,
    state: VpnThirdPartyState.View.CopyLogin,
): Flow<VpnThirdPartyEvent> {
    return flow {
        logger.logCopyEmail(itemId = state.data.accountId ?: "", domain = state.data.accountDomain ?: "")
        clipboardCopy.copyToClipboard(state.data.accountLogin ?: "", sensitiveData = false, showToast = false)
        emit(VpnThirdPartyEvent.Copied)
    }
}

fun copyPassword(
    clipboardCopy: ClipboardCopy,
    logger: VpnThirdPartyLogger,
    state: VpnThirdPartyState.View.CopyPassword,
): Flow<VpnThirdPartyEvent> {
    return flow {
        logger.logCopyPassword(itemId = state.data.accountId ?: "", domain = state.data.accountDomain ?: "")
        clipboardCopy.copyToClipboard(state.data.accountPassword ?: "", sensitiveData = true, showToast = false)
        emit(VpnThirdPartyEvent.Copied)
    }
}

fun logDownloadAppClick(
    repository: VpnThirdPartyRepository,
    logger: VpnThirdPartyLogger,
    state: VpnThirdPartyState.View.DownloadApp,
): Flow<VpnThirdPartyEvent> {
    return flow {
        logger.logClickDownload()
        val intent = repository.downloadIntent
        emit(VpnThirdPartyEvent.IntentLoaded(intent))
    }
}

fun getLaunchHotspotShieldIntent(
    repository: VpnThirdPartyRepository,
    state: VpnThirdPartyState.View.LaunchApp,
): Flow<VpnThirdPartyEvent> {
    return flow {
        val intent = repository.signInIntent
        emit(VpnThirdPartyEvent.IntentLoaded(intent))
    }
}