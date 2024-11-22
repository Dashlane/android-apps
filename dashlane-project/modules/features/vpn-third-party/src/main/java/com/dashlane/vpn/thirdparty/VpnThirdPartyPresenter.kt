package com.dashlane.vpn.thirdparty

import android.annotation.SuppressLint
import android.content.Intent
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.help.HelpCenterLink
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.utils.coroutines.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.vpn.thirdparty.VpnThirdPartyContract.DataProvider.Account
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class VpnThirdPartyPresenter @Inject constructor(
    @FragmentLifecycleCoroutineScope
    private val fragmentLifecycleCoroutineScope: CoroutineScope,
    private val logger: VpnThirdPartyLogger,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager
) :
    BasePresenter<VpnThirdPartyContract.DataProvider, VpnThirdPartyContract.ViewProxy>(),
    VpnThirdPartyContract.Presenter {

    private var account: Account? = null

    override fun onStart() {
        if (!preferencesManager[sessionManager.session?.username].isThirdPartyVpnInfoboxDismissed) view.showInfobox()
    }

    override fun onResume() {
        fragmentLifecycleCoroutineScope.launch(Dispatchers.Main) {
            account = provider.getHotspotShieldAccount()?.also {
                view.showAccount(it.login, it.password)
                if (provider.isHotspotShieldInstalled) view.showLaunchAppButton()
            }
            if (account == null) {
                if (!preferencesManager[sessionManager.session?.username].isThirdPartyVpnGetStartedDisplayed) {
                    view.showGettingStarted()
                }
                view.showActivate()
            }
            
            
            preferencesManager[sessionManager.session?.username].isThirdPartyVpnGetStartedDisplayed = true
        }
    }

    override fun onActivateClicked() = logger.logClickActivateAccount()

    override fun onInstallAppClicked() {
        logger.logClickDownload()
        startActivity(provider.installIntent)
    }

    override fun onLaunchAppClicked() {
        startActivity(provider.signInIntent)
    }

    override fun onDismissClicked() {
        preferencesManager[sessionManager.session?.username].isThirdPartyVpnInfoboxDismissed = true
    }

    override fun onLearnMoreClicked() {
        preferencesManager[sessionManager.session?.username].isThirdPartyVpnInfoboxDismissed = true
    }

    override fun onQuestionOneReadMoreClicked() =
        HelpCenterCoordinator.openLink(context!!, HelpCenterLink.ARTICLE_THIRD_PARTY_VPN)

    override fun onCopyLoginClicked() {
        account?.let { logger.logCopyEmail(it.itemId, it.domain) }
    }

    override fun onCopyPasswordClicked() {
        account?.let { logger.logCopyPassword(it.itemId, it.domain) }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startActivity(intent: Intent) {
        activity?.run {
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }
}