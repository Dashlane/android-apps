package com.dashlane.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.premiumstatus.planName
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.announcements.modules.KeyboardAutofillAnnouncementModule
import com.dashlane.braze.BrazeWrapper
import com.dashlane.databinding.ActivityHomeActivityLayoutBinding
import com.dashlane.debug.services.DaDaDaLogin
import com.dashlane.events.AppEvents
import com.dashlane.hardwaresecurity.SecurityHelper
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockType
import com.dashlane.navigation.NavigationConstants
import com.dashlane.navigation.Navigator
import com.dashlane.preference.PreferencesManager
import com.dashlane.premium.StoreOffersCache
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.fragments.vault.HiddenImpala
import com.dashlane.ui.dialogs.fragment.TeamRevokedDialogDisplayer
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.menu.MenuViewModel
import com.dashlane.ui.menu.menuScreen
import com.dashlane.ui.util.ActionBarUtil.DrawerLayoutProvider
import com.dashlane.ui.util.setup
import com.dashlane.ui.widgets.view.MainDrawerToggle
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.AppShortcutsUtil
import com.dashlane.util.DeviceUtils.hideKeyboard
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getParcelableExtraCompat
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : DashlaneActivity(), DrawerLayoutProvider, MenuContainer {
    @Inject
    lateinit var announcementCenter: AnnouncementCenter

    @Inject
    lateinit var appShortcutsUtil: AppShortcutsUtil

    @Inject
    lateinit var brazeWrapper: BrazeWrapper

    @Inject
    lateinit var storeOffersCache: StoreOffersCache

    @Inject
    lateinit var appEvents: AppEvents

    @Inject
    lateinit var securityHelper: SecurityHelper

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var dadadaLogin: DaDaDaLogin

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var teamSpaceRevokedDialogDisplayer: TeamRevokedDialogDisplayer

    @Inject
    lateinit var accountStatusRepository: AccountStatusRepository

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    @Inject
    lateinit var userAccountStorage: UserAccountStorage

    private lateinit var navigationDrawer: DrawerLayout
    private val menuViewModel: MenuViewModel by viewModels()
    private val viewModel: HomeViewModel by viewModels()

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
    }

    private var drawerToggle: MainDrawerToggle? = null
    var isResume = false
        private set

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val session = sessionManager.session
        if (session == null) {
            
            navigator.logoutAndCallLoginScreen(this)
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

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountStatusRepository.accountStatusState.collect { accountStatusState ->
                    accountStatusState[sessionManager.session]?.let {
                        onPremiumStatusChanged(it.premiumStatus.planName)
                        teamSpaceRevokedDialogDisplayer.onStatusChanged(WeakReference(this@HomeActivity))
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.stateFlow.sideEffect
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { sideEffect ->
                    when (sideEffect) {
                        is HomeState.SideEffect.InstallUpdate -> {
                            SnackbarUtils
                                .showSnackbar(this@HomeActivity, getString(R.string.app_update_needed), 10_000)
                                .setAction(R.string.app_update_needed_button) {
                                    appUpdateManager.startUpdateFlowForResult(
                                        sideEffect.appUpdateInfo,
                                        activityResultLauncher,
                                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                                    )
                                }
                        }
                    }
                }
        }

        
        HiddenImpala.configureForHomeActivity(this)

        setupSystemBarForAPI29()
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
                GravityCompat.START,
            )
        } else {
            navigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START)
        }
    }

    override fun getNavigationDrawer(): DrawerLayout {
        return navigationDrawer
    }

    private fun onPremiumStatusChanged(newSubscription: String?) {
        storeOffersCache.flushCacheIfSubscriptionHasChanged(newSubscription)
        loadAccessibleOffers()
        menuViewModel.refresh()
    }

    override fun onStart() {
        super.onStart()

        viewModel.viewStarted()

        if (dadadaLogin.isEnabled && dadadaLogin.hasDeepLink()) {
            val link = dadadaLogin.deepLink
            val builder = NotificationDialogFragment.Builder()
            builder.setNegativeButtonText("Cancel")
                .setPositiveButtonDeepLink(link)
                .setPositiveButtonText("Go")
                .setMessage("Go to \$link")
            builder.build().show(supportFragmentManager, link)
        }

        
        val username = sessionManager.session?.username
        if (username != null && !securityHelper.isDeviceSecured(username)) {
            val locks = lockManager.getLocks(username)
            val userAccountInfo = userAccountStorage[username]
            val hasPinLock = LockType.PinCode in locks
            val hasBiometricLock = LockType.Biometric in locks

            if (hasPinLock && userAccountInfo?.accountType is UserAccountInfo.AccountType.MasterPassword) {
                lockManager.removeLock(username, LockType.PinCode)
            }
            if (hasBiometricLock) {
                lockManager.removeLock(username, LockType.Biometric)
            }
            if (hasPinLock || hasBiometricLock) {
                securityHelper.showPopupPinCodeDisable(this)
            }
        }

        KeyboardAutofillAnnouncementModule.setRequireDisplayIfNeeded(preferencesManager[username], intent)
        announcementCenter.start(this)
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
        menuViewModel.refresh()
        viewModel.viewResumed()
    }

    override fun onPause() {
        super.onPause()
        isResume = false
    }

    override fun onStop() {
        super.onStop()
        
        appShortcutsUtil.refreshShortcuts(this)
        announcementCenter.stop(this)
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

    @Suppress("DEPRECATION")
    @SuppressWarnings("kotlin:S1874")
    private fun setupSystemBarForAPI29() {
        
        
        
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.statusBarColor = resources.getColor(R.color.container_agnostic_neutral_standard)
        }
    }
}