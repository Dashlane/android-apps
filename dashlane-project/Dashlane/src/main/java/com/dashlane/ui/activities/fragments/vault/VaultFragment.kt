package com.dashlane.ui.activities.fragments.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.notification.badge.NotificationBadgeActor
import com.dashlane.notification.badge.NotificationBadgeListener
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.fab.FabDef
import com.dashlane.ui.fab.FabPresenter
import com.dashlane.ui.fab.VaultFabViewProxy
import com.dashlane.util.inject.OptionalProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VaultFragment : AbstractContentFragment(), NotificationBadgeListener {
    @Inject
    lateinit var presenter: Vault.Presenter

    @Inject
    lateinit var notificationBadgeActor: NotificationBadgeActor

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var teamSpaceAccessor: OptionalProvider<TeamspaceAccessor>

    private var fabPresenter: FabDef.IPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val layout = inflater.inflate(R.layout.fragment_vault, container, false)

        presenter.also {
            it.setView(VaultViewProxy(this, layout))
            it.onCreate(arguments, savedInstanceState)
        }

        fabPresenter = FabPresenter(navigator).apply {
            val fabViewProxy = VaultFabViewProxy(
                rootView = layout,
                teamspaceManager = teamSpaceAccessor,
                navigator = navigator
            )
            setView(fabViewProxy)

            lifecycleScope.launch {
                presenter.filter.collect { filter ->
                    fabViewProxy.setFilter(filter)
                }
            }
        }

        notificationBadgeActor.subscribe(lifecycleScope, this)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabPresenter?.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fabPresenter?.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        presenter.onStartFragment()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStopFragment()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResumeFragment()
        lifecycleScope.launch {
            presenter.filter.collect { filter ->
                fabPresenter?.setFilter(filter)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_vault_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (notificationBadgeActor.hasUnread) {
            val alertMenu = menu.findItem(R.id.menu_alert)
            alertMenu.icon = resources.getDrawable(R.drawable.action_bar_menu_alert_with_dot, context?.theme)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_alert) {
            presenter.onMenuAlertClicked()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNotificationBadgeUpdated() {
        activity?.invalidateOptionsMenu()
    }
}