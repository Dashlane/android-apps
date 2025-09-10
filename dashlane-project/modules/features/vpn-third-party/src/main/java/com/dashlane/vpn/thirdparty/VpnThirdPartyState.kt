package com.dashlane.vpn.thirdparty

import android.content.Intent
import com.dashlane.mvvm.State
import com.dashlane.udf.Event

data class VpnThirdPartyData(
    val defaultEmail: String? = null,
    val userListSuggestion: List<String> = emptyList(),
    val accountLogin: String? = null,
    val accountPassword: String? = null,
    val accountId: String? = null,
    val accountDomain: String? = null,
    val showFaq: Boolean = false,
    val actionType: ActionType? = null,
) {
    enum class ActionType {
        DOWNLOAD_APP,
        LAUNCH_APP,
    }
}

sealed class VpnThirdPartyState : State {
    sealed class View : VpnThirdPartyState(), State.View {
        abstract val data: VpnThirdPartyData

        data class Initial(override val data: VpnThirdPartyData) : View()

        data class Activate(override val data: VpnThirdPartyData) : View()

        data class SetupVpn(override val data: VpnThirdPartyData) : View()

        data class Loading(override val data: VpnThirdPartyData) : View()

        data class Account(override val data: VpnThirdPartyData) : View()

        data class CopyLogin(override val data: VpnThirdPartyData) : View()

        data class CopyPassword(override val data: VpnThirdPartyData) : View()

        data class DownloadApp(override val data: VpnThirdPartyData) : View()

        data class LaunchApp(override val data: VpnThirdPartyData) : View()
    }

    sealed class SideEffect : VpnThirdPartyState(), State.SideEffect {
        data class GoToSetupVpn(val defaultEmail: String?, val userListSuggestion: List<String>) : SideEffect()

        data class DownloadApp(val intent: Intent) : SideEffect()

        data class LaunchApp(val intent: Intent) : SideEffect()

        data object GoToVpnIntroArticle : SideEffect()

        data object GoToVpnFaqArticle : SideEffect()
    }
}

sealed class VpnThirdPartyEvent : Event {
    data object ViewResumed : VpnThirdPartyEvent()

    data class AccountLoaded(
        val login: String,
        val password: String,
        val itemId: String,
        val domain: String,
        val isHotspotShieldInstalled: Boolean,
    ) : VpnThirdPartyEvent()

    data class NoAccountLoaded(
        val defaultEmail: String?,
        val userListSuggestion: List<String>,
    ) : VpnThirdPartyEvent()

    data object SetupVpnClick : VpnThirdPartyEvent()

    data object ShowFaq : VpnThirdPartyEvent()

    data object DismissFaq : VpnThirdPartyEvent()

    data object CopyLoginClick : VpnThirdPartyEvent()

    data object CopyPasswordClick : VpnThirdPartyEvent()

    data object Copied : VpnThirdPartyEvent()

    data object DownloadAppClick : VpnThirdPartyEvent()

    data object LaunchAppClick : VpnThirdPartyEvent()

    data class IntentLoaded(val intent: Intent) : VpnThirdPartyEvent()

    data object VpnIntroArticleClick : VpnThirdPartyEvent()

    data object VpnFaqArticleClick : VpnThirdPartyEvent()
}