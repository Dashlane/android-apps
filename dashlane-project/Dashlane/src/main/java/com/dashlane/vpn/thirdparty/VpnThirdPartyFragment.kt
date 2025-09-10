package com.dashlane.vpn.thirdparty

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.help.HelpCenterLink
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.setCurrentPageView
import com.dashlane.vpn.thirdparty.ui.VpnThirdPartyScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VpnThirdPartyFragment : AbstractContentFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(false)
        setCurrentPageView(AnyPage.TOOLS_VPN)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    VpnThirdPartyScreen(
                        viewModel = hiltViewModel(),
                        goToActivateThirdPartyVpn = ::goToActivateThirdPartyVpn,
                        installHotspotShieldApp = ::installHotspotShieldApp,
                        launchHotspotShieldApp = ::launchHotspotShieldApp,
                        goToHelpCenterPage = ::goToHelpCenterPage,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().invalidateOptionsMenu()
    }

    private fun goToActivateThirdPartyVpn(
        defaultEmail: String?,
        userListSuggestion: List<String>,
    ) {
        navigator.goToActivateAccountFromVpnThirdParty(defaultEmail, userListSuggestion)
    }

    private fun installHotspotShieldApp(installIntent: Intent) {
        startActivity(installIntent)
    }

    private fun launchHotspotShieldApp(signInIntent: Intent) {
        startActivity(signInIntent)
    }

    private fun goToHelpCenterPage(helpCenterLink: HelpCenterLink) {
        HelpCenterCoordinator.openLink(requireContext(), helpCenterLink)
    }
}