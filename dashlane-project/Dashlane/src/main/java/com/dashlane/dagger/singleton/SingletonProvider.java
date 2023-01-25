package com.dashlane.dagger.singleton;

import android.content.Context;

import com.dashlane.abtesting.OfflineExperimentReporter;
import com.dashlane.announcements.AnnouncementCenter;
import com.dashlane.autofill.api.monitorlog.MonitorAutofillIssuesSessionStartTrigger;
import com.dashlane.braze.BrazeWrapper;
import com.dashlane.breach.BreachManager;
import com.dashlane.core.DataSync;
import com.dashlane.core.sharing.SharingDao;
import com.dashlane.crashreport.CrashReporter;
import com.dashlane.events.AppEvents;
import com.dashlane.featureflipping.FeatureFlipManager;
import com.dashlane.inappbilling.BillingManager;
import com.dashlane.inapplogin.InAppLoginManager;
import com.dashlane.logger.AdjustWrapper;
import com.dashlane.login.lock.LockManager;
import com.dashlane.navigation.Navigator;
import com.dashlane.notification.FcmHelper;
import com.dashlane.notification.badge.NotificationBadgeActor;
import com.dashlane.notification.model.TokenJsonProvider;
import com.dashlane.passwordstrength.PasswordStrengthCache;
import com.dashlane.passwordstrength.PasswordStrengthEvaluator;
import com.dashlane.permission.PermissionsManager;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.preference.UserPreferencesManager;
import com.dashlane.premium.offer.common.OffersLogger;
import com.dashlane.security.SecurityHelper;
import com.dashlane.session.SessionManager;
import com.dashlane.session.SessionRestorer;
import com.dashlane.storage.securestorage.LocalKeyRepository;
import com.dashlane.storage.securestorage.UserSecureStorageManager;
import com.dashlane.storage.userdata.accessor.MainDataAccessor;
import com.dashlane.ui.GlobalActivityLifecycleListener;
import com.dashlane.ui.ScreenshotPolicy;
import com.dashlane.ui.premium.inappbilling.BillingVerification;
import com.dashlane.ui.premium.inappbilling.service.StoreOffersCache;
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider;
import com.dashlane.ui.util.DialogHelper;
import com.dashlane.update.AppUpdateInstaller;
import com.dashlane.useractivity.AggregateUserActivity;
import com.dashlane.usersupportreporter.UserSupportFileLogger;
import com.dashlane.debug.DaDaDa;
import com.dashlane.util.ThreadHelper;
import com.dashlane.util.Toaster;
import com.dashlane.util.clipboard.ClipboardCopy;
import com.dashlane.util.clipboard.vault.VaultItemFieldContentService;
import com.dashlane.util.notification.NotificationHelper;
import com.dashlane.util.userfeatures.UserFeaturesChecker;

import dagger.hilt.android.EntryPointAccessors;



public class SingletonProvider {

    private SingletonComponentProxy mComponent;

    protected SingletonProvider() {
        
    }

    public static Toaster getToaster() {
        return getComponent().getToaster();
    }

    public static ScreenshotPolicy getScreenshotPolicy() {
        return getComponent().getScreenshotPolicy();
    }

    public static LockManager getLockManager() {
        return getComponent().getLockManager();
    }

    public static PermissionsManager getPermissionsManager() {
        return getComponent().getPermissionsManager();
    }

    public static DialogHelper getDialogHelper() {
        return getComponent().getDialogHelper();
    }

    public static NotificationHelper getNotificationHelper() {
        return getComponent().getNotificationHelper();
    }

    public static UserSupportFileLogger getUserSupportFileLogger() {
        return getComponent().getUserSupportFileLogger();
    }

    public static PasswordStrengthEvaluator getPasswordStrengthEvaluator() {
        return getComponent().getPasswordStrengthEvaluator();
    }

    public static PasswordStrengthCache getPasswordStrengthCache() {
        return getComponent().getPasswordStrengthEvaluatorCache();
    }

    public static FeatureFlipManager getUserFeature() {
        return getComponent().getFeatureFlipManager();
    }

    public static UserFeaturesChecker getUserFeatureChecker() {
        return getComponent().getUserFeaturesChecker();
    }

    public static BreachManager getBreachManager() {
        return getComponent().getBreachManager();
    }

    public static SecurityHelper getSecurityHelper() {
        return getComponent().getSecurityHelper();
    }

    public static ThreadHelper getThreadHelper() {
        return getComponent().getThreadHelper();
    }

    public static CrashReporter getCrashReporter() {
        return getComponent().getCrashReporter();
    }

    public static AggregateUserActivity getAggregateUserActivity() {
        return getComponent().getAggregateUserActivity();
    }

    public static GlobalPreferencesManager getGlobalPreferencesManager() {
        return getComponent().getGlobalPreferencesManager();
    }

    public static UserPreferencesManager getUserPreferencesManager() {
        return getComponent().getUserPreferencesManager();
    }

    public static Context getContext() {
        return getComponent().getApplicationContext();
    }

    public static DaDaDa getDaDaDa() {
        return getComponent().getDaDaDa();
    }

    public static GlobalActivityLifecycleListener getGlobalActivityLifecycleListener() {
        return getComponent().getGlobalActivityLifecycleListener();
    }

    public static MainDataAccessor getMainDataAccessor() {
        return getComponent().getMainDataAccessor();
    }

    public static SharingDao getSharingDao() {
        return getComponent().getDataStorageProvider().getSharingDao();
    }

    public static BillingManager getBillingManager() {
        return getComponent().getBillingManager();
    }

    public static BillingVerification getBillingVerificator() {
        return getComponent().getBillingVerificator();
    }

    public static DataSync getDataSync() {
        return getComponent().getDataSync();
    }

    public static AdjustWrapper getAdjustWrapper() {
        return getComponent().getAdjustWrapper();
    }

    public static BrazeWrapper getBrazeWrapper() {
        return getComponent().getBrazeWrapper();
    }

    public static AnnouncementCenter getAnnouncementCenter() {
        return getComponent().getAnnouncementCenter();
    }

    public static InAppLoginManager getInAppLoginManager() {
        return getComponent().getInAppLoginManager();
    }

    public static TokenJsonProvider getTokenJsonProvider() {
        return getComponent().getTokenJsonProvider();
    }

    public static FcmHelper getFcmHelper() {
        return getComponent().getFcmHelper();
    }

    public static NotificationBadgeActor getNotificationBadgeActor() {
        return getComponent().getNotificationBadgeActor();
    }

    public static Navigator getNavigator() {
        return getComponent().getNavigator();
    }

    public static OffersLogger getOffersLogger() {
        return getComponent().getOffersLogger();
    }

    public static MonitorAutofillIssuesSessionStartTrigger getMonitorAutofillIssuesSessionStartTrigger() {
        return new MonitorAutofillIssuesSessionStartTrigger(
                getComponent().getMonitorAutofillIssues(),
                getComponent().getMonitorAutofillIssuesLogger(),
                getComponent().getSessionCoroutineScopeRepository()
        );
    }

    public static void setSingletonComponent(SingletonComponentProxy component) {
        SingletonHolder.INSTANCE.mComponent = component;
    }

    public static void reset() {
        setSingletonComponent(null);
    }

    public static SingletonComponentProxy getComponent() {
        return SingletonHolder.INSTANCE.mComponent;
    }

    public static void init(Context context) {
        if (SingletonHolder.INSTANCE.mComponent == null) {
            setSingletonComponent(EntryPointAccessors.fromApplication(
                    context,
                    SingletonComponentProxy.class
            ));
        }
    }

    public static StoreOffersCache getAccessibleOffersCache() {
        return getComponent().getAccessibleOffersCache();
    }

    public static UserSecureStorageManager getUserSecureDataStorageManager() {
        return getComponent().getSecureDataStoreManager();
    }

    public static OfflineExperimentReporter getOfflineExperimentsReporter() {
        return getComponent().getOfflineExperimentsReporter();
    }

    public static SessionManager getSessionManager() {
        return getComponent().getSessionManager();
    }

    public static SessionRestorer getSessionRestorer() {
        return getComponent().getSessionRestorer();
    }

    public static AppEvents getAppEvents() {
        return getComponent().getAppEvents();
    }

    public static AppUpdateInstaller getAppUpdateInstaller() {
        return getComponent().getAppUpdateInstaller();
    }

    public static LocalKeyRepository getLocalKeyRepository() {
        return getComponent().getLocalKeyRepository();
    }

    public static ClipboardCopy getClipboardCopy() {
        return getComponent().getClipboardCopy();
    }

    public static VaultItemFieldContentService getVaultItemFieldContentService() {
        return getComponent().getVaultItemFieldContentService();
    }

    public static SharingPolicyDataProvider getSharingPolicyDataProvider() {
        return getComponent().getSharingPolicyDataProvider();
    }

    private static class SingletonHolder {
        private static final SingletonProvider INSTANCE = new SingletonProvider();
    }
}
