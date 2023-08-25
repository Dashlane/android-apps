package com.dashlane.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import com.dashlane.R;
import com.dashlane.abtesting.RemoteAbTestManager;
import com.dashlane.analytics.metrics.time.SpentTimeOnViewManager;
import com.dashlane.announcements.modules.KeyboardAutofillAnnouncementModule;
import com.dashlane.braze.BrazeWrapper;
import com.dashlane.core.DataSync;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.debug.DaDaDa;
import com.dashlane.events.AppEvents;
import com.dashlane.events.PremiumStatusChangedEvent;
import com.dashlane.login.lock.LockManager;
import com.dashlane.navigation.NavigationConstants;
import com.dashlane.navigation.NavigationUtils;
import com.dashlane.session.Session;
import com.dashlane.ui.activities.fragments.vault.HiddenImpala;
import com.dashlane.ui.dialogs.fragment.SpaceRevokedDialog;
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment;
import com.dashlane.ui.menu.DashlaneMenuView;
import com.dashlane.ui.util.ActionBarUtil;
import com.dashlane.ui.util.DrawerLayoutUtilsKt;
import com.dashlane.ui.widgets.view.MainDrawerToggle;
import com.dashlane.util.AppShortcutsUtil;
import com.dashlane.util.DeviceUtils;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleOwnerKt;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends DashlaneActivity
        implements ActionBarUtil.DrawerLayoutProvider, MenuContainer {

    private static final String SAVED_STATE_HAS_CHANGED_CONFIGURATIONS = "saved_state_has_changed_configurations";

    @NonNull
    protected DrawerLayout mNavigationDrawer;
    private MainDrawerToggle mDrawerToggle;
    private DashlaneMenuView mMenuFrame;
    private boolean mIsResume;
    private LockManager mLockManager;

    @Inject
    RemoteAbTestManager remoteAbTestManager;

    @Inject
    AppShortcutsUtil appShortcutsUtil;

    @Inject
    DataSync mDataSync;

    @Inject
    BrazeWrapper brazeWrapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        configureAndroidActivityFeatures();
        super.onCreate(savedInstanceState);
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session == null) {
            
            NavigationUtils.logoutAndCallLoginScreen(this, false);
            return;
        }

        mLockManager = SingletonProvider.getComponent().getLockRepository()
                .getLockManager(session);

        setContentView(R.layout.activity_home_activity_layout);
        mNavigationDrawer = findViewById(R.id.drawer_layout);
        mMenuFrame = findViewById(R.id.menu_frame);

        initializeNavigationDrawer();

        
        HiddenImpala.configureForHomeActivity(this);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        handleDeepLink(getIntent());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDeepLink(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return SingletonProvider.getNavigator().navigateUp();
    }

    private void forceCloseNavigationDrawer(int gravity) {
        mNavigationDrawer.closeDrawer(gravity);
    }

    @Override
    public boolean isNavigationDrawerVisible() {
        return mNavigationDrawer.isDrawerOpen(GravityCompat.START);
    }

    @Override
    public void disableMenuAccess(boolean bool) {
        if (bool) {
            mNavigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
        } else {
            mNavigationDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (isNavigationDrawerVisible()) {
            forceCloseNavigationDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public DrawerLayout getNavigationDrawer() {
        return mNavigationDrawer;
    }

    private void onPremiumStatusChanged(PremiumStatusChangedEvent event) {
        SingletonProvider.getAppEvents().clearLastEvent(PremiumStatusChangedEvent.class);
        if (event.premiumPlanChanged()) {
            SingletonProvider.getAccessibleOffersCache().flushCache();
            loadAccessibleOffers();
            mMenuFrame.refresh();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SingletonProvider.getUserFeature().launchRefreshIfNeeded();

        remoteAbTestManager.launchRefreshIfNeeded();

        
        SingletonProvider.getOfflineExperimentsReporter().launchReportIfNeeded();

        SingletonProvider.getBillingVerificator().verifyAndConsumePurchaseIfNeeded();

        final DaDaDa daDaDa = SingletonProvider.getDaDaDa();
        if (daDaDa.isEnabled() && daDaDa.hasDeepLink()) {
            String link = daDaDa.getDeepLink();
            NotificationDialogFragment.Builder builder = new NotificationDialogFragment.Builder();
            builder.setNegativeButtonText("Cancel")
                   .setPositiveButtonDeepLink(link)
                   .setPositiveButtonText("Go")
                   .setMessage("Go to $link");
            builder.build().show(getSupportFragmentManager(), link);
        }

        SingletonProvider.getSecurityHelper().showPopupPinCodeDisableIfWasEnable(this);

        SingletonProvider.getComponent().getDeviceUpdateManager().updateIfNeeded();

        KeyboardAutofillAnnouncementModule.Companion
                .setRequireDisplayIfNeeded(SingletonProvider.getUserPreferencesManager(), getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        brazeWrapper.openSession(this);

        
        if (isFinishing()) {
            mIsResume = false;
            return;
        }
        mIsResume = true;
        if (isNavigationDrawerVisible()) {
            DeviceUtils.hideKeyboard(mNavigationDrawer);
        }
        final AppEvents appEvents = SingletonProvider.getAppEvents();
        appEvents.register(this, PremiumStatusChangedEvent.class, true, premiumStatusChangedEvent -> {
            onPremiumStatusChanged(premiumStatusChangedEvent);
            return null;
        });

        if (!mLockManager.isLocked()) {
            SpentTimeOnViewManager.getInstance().enterView(getLocalClassName());
        }

        SpaceRevokedDialog.showIfNecessary(this, getSupportFragmentManager());
        SpaceRevokedDialog.listenUpcoming(this);
        mMenuFrame.refresh();
        loadAccessibleOffers();
        mDataSync.maySync();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_STATE_HAS_CHANGED_CONFIGURATIONS, isChangingConfigurations());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsResume) {
            
            AppEvents appEvents = SingletonProvider.getAppEvents();
            appEvents.unregister(this, PremiumStatusChangedEvent.class);
            if (SingletonProvider.getSessionManager().getSession() != null) {
                SpentTimeOnViewManager.getInstance().leaveView(getLocalClassName());
            }
        }
        mIsResume = false;
        SpaceRevokedDialog.stopUpcomingListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        
        appShortcutsUtil.refreshShortcuts(this);
    }

    public boolean isResume() {
        return mIsResume;
    }

    private void initializeNavigationDrawer() {
        ActionBarUtil actionBarUtil = getActionBarUtil();
        actionBarUtil.setup();
        mDrawerToggle =
                new MainDrawerToggle(this, mNavigationDrawer, actionBarUtil.getToolbar());
        DrawerLayoutUtilsKt.setup(mNavigationDrawer, mDrawerToggle, actionBarUtil, mMenuFrame);
        SingletonProvider
                .getNavigator()
                .addOnDestinationChangedListener(
                        (controller, destination, arguments) -> forceCloseNavigationDrawer(GravityCompat.START)
                );
    }

    private void configureAndroidActivityFeatures() {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

    private void loadAccessibleOffers() {
        SingletonProvider.getAccessibleOffersCache()
                .prefetchProductsForCurrentUser(LifecycleOwnerKt.getLifecycleScope(this));
    }

    private void handleDeepLink(Intent intent) {
        if (intent != null && intent.hasExtra(NavigationConstants.STARTED_WITH_INTENT)) {
            Intent startIntent = intent.getParcelableExtra(NavigationConstants.STARTED_WITH_INTENT);
            if (startIntent != null) {
                SingletonProvider.getNavigator().handleDeepLink(startIntent);
                intent.removeExtra(NavigationConstants.STARTED_WITH_INTENT);
                setIntent(intent);
            }
        }
    }
}