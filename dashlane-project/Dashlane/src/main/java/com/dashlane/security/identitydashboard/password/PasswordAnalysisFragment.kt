package com.dashlane.security.identitydashboard.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.security.identitydashboard.password.PasswordAnalysisFragmentArgs.Companion.fromBundle
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.util.Toaster
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PasswordAnalysisFragment : AbstractContentFragment() {
    @Inject
    lateinit var dataProvider: PasswordAnalysisDataProvider

    @Inject
    lateinit var toaster: Toaster

    @Inject
    lateinit var vaultDataQuery: VaultDataQuery

    @Inject
    lateinit var itemWrapperProvider: ItemWrapperProvider

    @Inject
    lateinit var currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var accountStatusRepository: AccountStatusRepository

    private lateinit var presenter: PasswordAnalysisPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard_password_analysis, container, false)
        val viewProxy: PasswordAnalysisContract.ViewProxy = PasswordAnalysisViewProxy(view)

        presenter = PasswordAnalysisPresenter(
            coroutineScope = lifecycleScope,
            toaster = toaster,
            navigator = navigator,
            vaultDataQuery = vaultDataQuery,
            itemWrapperProvider = itemWrapperProvider,
            currentTeamSpaceUiFilter = currentTeamSpaceUiFilter,
            sessionManager = sessionManager,
            accountStatusRepository = accountStatusRepository
        )

        arguments?.let {
            val (tab, breachFocus) = fromBundle(it)
            presenter.defaultDestination = tab
            if (savedInstanceState == null) {
                presenter.focusBreachIdPending = breachFocus
            }
        }
        presenter.setProvider(dataProvider)
        presenter.setView(viewProxy)
        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.onViewVisible()
    }

    override fun onPause() {
        super.onPause()
        presenter.onViewHidden()
    }
}
