package com.dashlane.ui.activities.fragments.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.limitations.PasswordLimiter
import com.dashlane.notification.badge.NotificationBadgeActor
import com.dashlane.notification.badge.NotificationBadgeListener
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.fab.FabDef
import com.dashlane.ui.fab.FabPresenter
import com.dashlane.ui.fab.VaultFabViewProxy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VaultFragment : AbstractContentFragment(), NotificationBadgeListener {
    @Inject
    lateinit var presenter: Vault.Presenter

    @Inject
    lateinit var notificationBadgeActor: NotificationBadgeActor

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var passwordLimiter: PasswordLimiter

    @Inject
    lateinit var featuresChecker: UserFeaturesChecker

    @Inject
    lateinit var teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.fragment_vault_menu, menu)
            if (notificationBadgeActor.hasUnread) {
                val alertMenu = menu.findItem(R.id.menu_alert)
                alertMenu.icon = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.action_bar_menu_alert_with_dot,
                    context?.theme
                )
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (menuItem.itemId == R.id.menu_alert) {
                presenter.onMenuAlertClicked()
                return true
            }
            return false
        }
    }

    private var fabPresenter: FabDef.IPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val layout = inflater.inflate(R.layout.fragment_vault, container, false)

        presenter.also {
            it.setView(VaultViewProxy(view = layout, fragment = this))
            it.onCreate(arguments, savedInstanceState)
        }

        fabPresenter = FabPresenter(navigator).apply {
            val fabViewProxy = VaultFabViewProxy(
                rootView = layout,
                teamspaceRestrictionNotificator = teamspaceRestrictionNotificator,
                navigator = navigator,
                passwordLimiter = passwordLimiter,
                featuresChecker = featuresChecker
            )
            setView(fabViewProxy)

            lifecycleScope.launch {
                presenter.filter.collect { filter ->
                    fabViewProxy.setFilter(filter)
                }
            }
        }
        requireActivity().addMenuProvider(menuProvider)

        notificationBadgeActor.subscribe(lifecycleScope, this)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabPresenter?.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().removeMenuProvider(menuProvider)
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

    override fun onNotificationBadgeUpdated() {
        activity?.invalidateOptionsMenu()
    }
}