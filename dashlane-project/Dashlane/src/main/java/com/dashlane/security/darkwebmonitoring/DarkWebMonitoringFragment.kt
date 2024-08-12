package com.dashlane.security.darkwebmonitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.databinding.FragmentDarkwebMonitoringBinding
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DarkWebMonitoringFragment : AbstractContentFragment() {

    @Inject
    lateinit var presenter: DarkWebMonitoringContract.Presenter

    @Inject
    lateinit var accountStatusRepository: AccountStatusRepository

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountStatusRepository.accountStatusState.collect { accountStatusState ->
                    accountStatusState[sessionManager.session]?.let {
                        presenter.requireRefresh()
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setCurrentPageView(AnyPage.TOOLS_DARK_WEB_MONITORING)
        val binding = FragmentDarkwebMonitoringBinding.inflate(layoutInflater, container, false)
        val viewProxy: DarkWebMonitoringContract.ViewProxy =
            DarkWebMonitoringViewProxy(binding, requireActivity() as DashlaneActivity)
        presenter.setView(viewProxy)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        presenter.onCreateOptionsMenu(inflater, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        presenter.onViewVisible()
    }

    override fun onStop() {
        super.onStop()
        presenter.onViewHidden()
    }
}