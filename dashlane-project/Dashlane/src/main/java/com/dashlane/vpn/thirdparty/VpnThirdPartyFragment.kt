package com.dashlane.vpn.thirdparty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dashlane.R
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.clipboard.ClipboardCopy
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

    @Inject
    lateinit var clipboardCopy: ClipboardCopy

    @Inject
    lateinit var mainDataAccessor: MainDataAccessor

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
        val dataProvider = VpnThirdPartyDataProvider(
            activity.packageManager,
            mainDataAccessor.getCredentialDataQuery(),
            mainDataAccessor.getVaultDataQuery()
        )
        presenter.setView(
            VpnThirdPartyViewProxy(
                this,
                navigator,
                clipboardCopy,
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