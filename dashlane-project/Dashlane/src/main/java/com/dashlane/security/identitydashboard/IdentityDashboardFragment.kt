package com.dashlane.security.identitydashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.R
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IdentityDashboardFragment : AbstractContentFragment() {
    @Inject
    lateinit var dataProvider: IdentityDashboardDataProvider

    @Inject
    lateinit var presenter: IdentityDashboardPresenter

    @Inject
    lateinit var accountStatusRepository: AccountStatusRepository

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter

    @Inject
    @ApplicationCoroutineScope
    lateinit var applicationCoroutineScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountStatusRepository.accountStatusState.collect { accountStatuses ->
                    accountStatuses[sessionManager.session]?.let {
                        presenter.requireRefresh(forceRefresh = true)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.setCurrentPageView(AnyPage.TOOLS_PASSWORD_HEALTH_OVERVIEW)
        val view = inflater.inflate(R.layout.fragment_dashboard_identity, container, false)
        val viewProxy = IdentityDashboardViewProxy(view)
        presenter.setProvider(dataProvider)
        presenter.setView(viewProxy)
        val scope = if (activity is DashlaneActivity) {
            (activity as DashlaneActivity?)!!.lifecycleScope
        } else {
            applicationCoroutineScope
        }
        presenter.coroutineScope = scope
        dataProvider.coroutineScope = scope
        return view
    }

    override fun onStart() {
        super.onStart()
        presenter.onViewVisible()
        lifecycleScope.launch {
            currentTeamSpaceUiFilter.teamSpaceFilterState.collect {
                presenter.requireRefresh(forceRefresh = true)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.onViewHidden()
    }
}
