package com.dashlane.vpn.thirdparty.activate

import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.help.HelpCenterLink
import com.dashlane.server.api.endpoints.vpn.exceptions.UserAlreadyHasAnAccountForProviderException
import com.dashlane.util.isValidEmail
import com.dashlane.util.launchUrl
import com.dashlane.vpn.thirdparty.R
import com.dashlane.vpn.thirdparty.VpnThirdPartyLogger
import com.dashlane.vpn.thirdparty.activate.VpnThirdPartyActivateAccountEmailErrorFragmentDirections.Companion.emailErrorToSetupEmail
import com.dashlane.vpn.thirdparty.activate.VpnThirdPartyActivateAccountErrorFragmentDirections.Companion.errorToLoading
import com.dashlane.vpn.thirdparty.activate.VpnThirdPartyActivateAccountLoadingFragmentDirections.Companion.loadingToAccountError
import com.dashlane.vpn.thirdparty.activate.VpnThirdPartyActivateAccountLoadingFragmentDirections.Companion.loadingToError
import com.dashlane.vpn.thirdparty.activate.VpnThirdPartyActivateAccountLoadingFragmentDirections.Companion.loadingToSuccess
import com.dashlane.vpn.thirdparty.activate.VpnThirdPartySetupEmailFragmentDirections.Companion.setupToLoading
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.delay

class VpnThirdPartyActivateAccountPresenter(
    private val helpCenterCoordinator: HelpCenterCoordinator,
    private val logger: VpnThirdPartyLogger
) :
    BasePresenter<VpnThirdPartyActivateAccountContract.DataProvider, VpnThirdPartyActivateAccountContract.ViewProxy>(),
    VpnThirdPartyActivateAccountContract.Presenter {
    private val navigator: NavController
        get() = activity!!.findNavController(R.id.nav_host_vpn_third_party_activate_account)
    private lateinit var lastEmail: String

    override fun isEmailValid(email: String) = email.isNotEmpty() && email.isValidEmail()

    override suspend fun onConfirmClicked(email: String) {
        lastEmail = email
        navigator.navigate(setupToLoading())
        createAccount(email)
    }

    override fun onLearnMoreClicked() {
        context?.let {
            helpCenterCoordinator.openLink(it, HelpCenterLink.ARTICLE_THIRD_PARTY_VPN_HOW_TO, true)
        }
    }

    override suspend fun onTryAgain() {
        navigator.navigate(errorToLoading())
        createAccount(lastEmail)
    }

    override fun onTryAgainAccountExists() {
        navigator.navigate(emailErrorToSetupEmail())
    }

    override fun onContactSupport() {
        context?.launchUrl("https://support.dashlane.com/hc/requests/new")
    }

    override fun onContactProviderSupport() {
        context?.launchUrl("https://support.hotspotshield.com/hc/requests/new")
    }

    private suspend fun createAccount(email: String) {
        try {
            val password = provider.createHotspotShieldAccount(email).data.password
            provider.saveAccount(
                context!!.getString(R.string.vpn_third_party_hotspot_shield_name),
                email,
                password
            )
            navigator.navigate(loadingToSuccess())
            logger.logActivated()
            activity?.let {
                delay(3_000L)
                it.finish()
            }
        } catch (e: Exception) {
            when (e) {
                is UserAlreadyHasAnAccountForProviderException -> {
                    logger.logEmailInUseActivationError()
                    navigator.navigate(loadingToAccountError())
                }
                else -> {
                    logger.logServerActivationError()
                    navigator.navigate(loadingToError())
                }
            }
        }
    }
}