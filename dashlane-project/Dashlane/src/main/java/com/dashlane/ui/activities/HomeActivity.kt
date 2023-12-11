package com.dashlane.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.abtesting.OfflineExperimentReporter
import com.dashlane.abtesting.RemoteAbTestManager
import com.dashlane.announcements.modules.KeyboardAutofillAnnouncementModule
import com.dashlane.braze.BrazeWrapper
import com.dashlane.core.DataSync
import com.dashlane.databinding.ActivityHomeActivityLayoutBinding
import com.dashlane.debug.DaDaDa
import com.dashlane.device.DeviceUpdateManager
import com.dashlane.events.AppEvents
import com.dashlane.events.PremiumStatusChangedEvent
import com.dashlane.featureflipping.FeatureFlipManager
import com.dashlane.navigation.NavigationConstants
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.SecurityHelper
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.fragments.vault.HiddenImpala
import com.dashlane.ui.dialogs.fragment.TeamRevokedDialogDisplayer
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.menu.MenuViewModel
import com.dashlane.ui.menu.menuScreen
import com.dashlane.ui.premium.inappbilling.BillingVerification
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache
import com.dashlane.ui.util.ActionBarUtil.DrawerLayoutProvider
import com.dashlane.ui.util.setup
import com.dashlane.ui.widgets.view.MainDrawerToggle
import com.dashlane.util.AppShortcutsUtil
import com.dashlane.util.DeviceUtils.hideKeyboard
import com.dashlane.util.getParcelableExtraCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : DashlaneActivity(), DrawerLayoutProvider, MenuContainer {
    @Inject
    lateinit var remoteAbTestManager: RemoteAbTestManager

    @Inject
    lateinit var appShortcutsUtil: AppShortcutsUtil

    @Inject
    lateinit var mDataSync: DataSync

    @Inject
    lateinit var brazeWrapper: BrazeWrapper

    @Inject
    lateinit var offlineExperimentReporter: OfflineExperimentReporter

    @Inject
    lateinit var featureFlipManager: FeatureFlipManager

    @Inject
    lateinit var billingVerification: BillingVerification

    @Inject
    lateinit var storeOffersCache: StoreOffersCache

    @Inject
    lateinit var appEvents: AppEvents

    @Inject
    lateinit var securityHelper: SecurityHelper

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var dadada: DaDaDa

    @Inject
    lateinit var deviceUpdateManager: DeviceUpdateManager

    @Inject
    lateinit var userPreferenceManager: UserPreferencesManager

    @Inject
    lateinit var teamSpaceRevokedDialogListener: TeamRevokedDialogDisplayer

    private lateinit var navigationDrawer: DrawerLayout
    private val menuViewModel: MenuViewModel by viewModels()
    private var drawerToggle: MainDrawerToggle? = null
    var isResume = false
        private set

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val session = sessionManager.session
        if (session == null) {
            
            navigator.logoutAndCallLoginScreen(this, false)
            return
        }
        val homeViewBinding = ActivityHomeActivityLayoutBinding.inflate(layoutInflater)
        setContentView(homeViewBinding.root)
        navigationDrawer = homeViewBinding.drawerLayout
        menuScreen(homeViewBinding, menuViewModel)
        navigator.addOnDestinationChangedListener(menuViewModel)
        initializeNavigationDrawer()

        
        val drawerCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isNavigationDrawerVisible()) {
                    forceCloseNavigationDrawer()
                }
            }
        }
        navigationDrawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                drawerCallback.remove()
            }

            override fun onDrawerOpened(drawerView: View) {
                onBackPressedDispatcher.addCallback(this@HomeActivity, drawerCallback)
            }
        })

        
        HiddenImpala.configureForHomeActivity(this)
    }

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        handleDeepLink(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        
        
        return if (drawerToggle?.onOptionsItemSelected(item) == true) {
            true
        } else {
            
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navigator.navigateUp()
    }

    private fun forceCloseNavigationDrawer() {
        navigationDrawer.closeDrawer(GravityCompat.START)
    }

    override fun isNavigationDrawerVisible(): Boolean {
        return navigationDrawer.isDrawerOpen(GravityCompat.START)
    }

    override fun disableMenuAccess(bool: Boolean) {
        if (bool) {
            navigationDrawer.setDrawerLockMode(
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                GravityCompat.START
            )
        } else {
            navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START)
        }
    }

    override fun getNavigationDrawer(): DrawerLayout {
        return navigationDrawer
    }

    private fun onPremiumStatusChanged(event: PremiumStatusChangedEvent) {
        appEvents.clearLastEvent(PremiumStatusChangedEvent::class.java)
        if (event.premiumPlanChanged()) {
            storeOffersCache.flushCache()
            loadAccessibleOffers()
            menuViewModel.refresh()
        }
    }

    override fun onStart() {
        super.onStart()
        featureFlipManager.launchRefreshIfNeeded()
        remoteAbTestManager.launchRefreshIfNeeded()

        
        offlineExperimentReporter.launchReportIfNeeded()
        billingVerification.verifyAndConsumePurchaseIfNeeded()
        if (dadada.isEnabled && dadada.hasDeepLink()) {
            val link = dadada.deepLink
            val builder = NotificationDialogFragment.Builder()
            builder.setNegativeButtonText("Cancel")
                .setPositiveButtonDeepLink(link)
                .setPositiveButtonText("Go")
                .setMessage("Go to \$link")
            builder.build().show(supportFragmentManager, link)
        }
        securityHelper.showPopupPinCodeDisableIfWasEnable(this)
        deviceUpdateManager.updateIfNeeded()
        KeyboardAutofillAnnouncementModule.setRequireDisplayIfNeeded(userPreferenceManager, intent)
    }

    override fun onResume() {
        super.onResume()
        brazeWrapper.openSession(this)

        
        if (isFinishing) {
            isResume = false
            return
        }
        isResume = true
        if (isNavigationDrawerVisible()) {
            hideKeyboard(navigationDrawer)
        }
        appEvents.register(
            this,
            PremiumStatusChangedEvent::class.java,
            true
        ) { premiumStatusChangedEvent: PremiumStatusChangedEvent ->
            onPremiumStatusChanged(premiumStatusChangedEvent)
        }
        teamSpaceRevokedDialogListener.showIfNecessary(this, supportFragmentManager)
        teamSpaceRevokedDialogListener.listenUpcoming(this)
        menuViewModel.refresh()
        loadAccessibleOffers()
        mDataSync.maySync()
    }

    override fun onPause() {
        super.onPause()
        if (isResume) {
            
            appEvents.unregister(this, PremiumStatusChangedEvent::class.java)
        }
        isResume = false
        teamSpaceRevokedDialogListener.stopUpcomingListener()
    }

    override fun onStop() {
        super.onStop()
        
        appShortcutsUtil.refreshShortcuts(this)
    }

    private fun initializeNavigationDrawer() {
        actionBarUtil.setup()
        drawerToggle = MainDrawerToggle(this, navigationDrawer, actionBarUtil.toolbar)
        navigationDrawer.setup(drawerToggle!!, actionBarUtil, findViewById(R.id.menu_frame))
        navigator.addOnDestinationChangedListener { _, _, _ ->
            forceCloseNavigationDrawer()
        }
    }

    private fun loadAccessibleOffers() {
        storeOffersCache.prefetchProductsForCurrentUser(this.lifecycleScope)
    }

    private fun handleDeepLink(intent: Intent?) {
        if (intent != null && intent.hasExtra(NavigationConstants.STARTED_WITH_INTENT)) {
            val startIntent = intent.getParcelableExtraCompat<Intent>(NavigationConstants.STARTED_WITH_INTENT)
            if (startIntent != null) {
                navigator.handleDeepLink(startIntent)
                intent.removeExtra(NavigationConstants.STARTED_WITH_INTENT)
                setIntent(intent)
            }
        }
    }
}