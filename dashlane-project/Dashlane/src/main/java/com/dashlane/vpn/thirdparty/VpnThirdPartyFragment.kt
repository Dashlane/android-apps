package com.dashlane.vpn.thirdparty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VpnThirdPartyFragment : AbstractContentFragment() {

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager
    @Inject
    lateinit var preferences: GlobalPreferencesManager
    @Inject
    lateinit var helpCenterCoordinator: HelpCenterCoordinator

    @Inject
    lateinit var presenter: VpnThirdPartyPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(false)
        setCurrentPageView(AnyPage.TOOLS_VPN)
        return inflater.inflate(R.layout.fragment_vpn_third_party, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        val dataAccessor = SingletonProvider.getMainDataAccessor()
        val dataProvider = VpnThirdPartyDataProvider(
            activity.packageManager,
            dataAccessor.getCredentialDataQuery(),
            dataAccessor.getVaultDataQuery()
        )
        presenter.setView(
            VpnThirdPartyViewProxy(
                this,
                SingletonProvider.getNavigator(),
                SingletonProvider.getClipboardCopy(),
                preferences.getLastLoggedInUser(),
                preferences.getUserListHistory()
            )
        )
        presenter.setProvider(dataProvider)
        activity.invalidateOptionsMenu()
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }
}