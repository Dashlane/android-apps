package com.dashlane.vpn.thirdparty.activate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.inject.HermesComponent
import com.dashlane.server.api.endpoints.vpn.VpnGetCredentialsService
import com.dashlane.session.SessionManager
import com.dashlane.util.setCurrentPageView
import com.dashlane.vpn.thirdparty.R
import com.dashlane.vpn.thirdparty.VpnThirdPartyAuthentifiantHelper
import com.dashlane.vpn.thirdparty.VpnThirdPartyLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class VpnThirdPartySetupEmailFragment : Fragment() {

    @Inject
    lateinit var helpCenterCoordinator: HelpCenterCoordinator
    @Inject
    lateinit var getVpnCredentialsService: VpnGetCredentialsService
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var authentifiantHelper: VpnThirdPartyAuthentifiantHelper

    interface Listener {
        fun onPresenterReady(presenter: VpnThirdPartyActivateAccountPresenter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vpn_third_party_setup_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentPageView(AnyPage.TOOLS_VPN_PRIVACY_CONSENT)
        val logger = VpnThirdPartyLogger(HermesComponent(requireActivity()).logRepository)
        val presenter =
            VpnThirdPartyActivateAccountPresenter(helpCenterCoordinator, logger)
        val defaultEmail = activity?.intent?.getStringExtra("email")
        val suggestions = activity?.intent?.getStringArrayExtra("suggestions")
        presenter.setView(
            VpnThirdPartyActivateAccountViewProxy(this, defaultEmail, suggestions?.toList())
        )
        presenter.setProvider(
            VpnThirdPartyActivateAccountDataProvider(
                getVpnCredentialsService,
                sessionManager,
                authentifiantHelper
            )
        )
        (activity as? Listener)?.onPresenterReady(presenter)
    }
}