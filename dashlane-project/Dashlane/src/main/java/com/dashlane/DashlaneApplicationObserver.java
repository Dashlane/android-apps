package com.dashlane;

import android.app.Application;
import android.app.backup.BackupManager;
import android.content.Context;
import android.os.Looper;

import com.dashlane.abtesting.OfflineExperimentReporter;
import com.dashlane.account.UserAccountStorage;
import com.dashlane.accountrecovery.AccountRecovery;
import com.dashlane.announcements.AnnouncementCenter;
import com.dashlane.async.BroadcastManager;
import com.dashlane.async.InstallReporter;
import com.dashlane.autofill.api.linkedservices.AppMetaDataToLinkedAppsMigration;
import com.dashlane.autofill.api.linkedservices.RememberToLinkedAppsMigration;
import com.dashlane.autofill.api.monitorlog.MonitorAutofillIssuesSessionStartTrigger;
import com.dashlane.callbacks.AdjustActivityCallback;
import com.dashlane.core.DataSync;
import com.dashlane.core.helpers.DashlaneHelper;
import com.dashlane.crashreport.CrashReporter;
import com.dashlane.dagger.singleton.SingletonComponentProxy;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.db.UpdateManager;
import com.dashlane.debug.DeveloperUtilities;
import com.dashlane.device.DeviceUpdateManager;
import com.dashlane.endoflife.EndOfLifeObserver;
import com.dashlane.inapplogin.InAppLoginManager;
import com.dashlane.notification.FcmHelper;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.preference.UserPreferencesManager;
import com.dashlane.server.api.DashlaneApi;
import com.dashlane.session.BySessionRepository;
import com.dashlane.session.SessionManager;
import com.dashlane.session.SessionRestorerUtilsKt;
import com.dashlane.session.observer.AnnouncementCenterObserver;
import com.dashlane.session.observer.BackupTokenObserver;
import com.dashlane.session.observer.BroadcastManagerObserver;
import com.dashlane.session.observer.CryptographyMigrationObserver;
import com.dashlane.session.observer.DeviceUpdateManagerObserver;
import com.dashlane.session.observer.FcmRegistrationObserver;
import com.dashlane.session.observer.LinkedAppsMigrationObserver;
import com.dashlane.session.observer.LogsObserver;
import com.dashlane.session.observer.OfflineExperimentObserver;
import com.dashlane.session.observer.RacletteLoggerObserver;
import com.dashlane.session.observer.SyncObserver;
import com.dashlane.session.observer.SystemUpdateObserver;
import com.dashlane.session.observer.UserSettingsLogObserver;
import com.dashlane.session.repository.AccountStatusRepository;
import com.dashlane.session.repository.LockRepository;
import com.dashlane.session.repository.SessionCoroutineScopeRepository;
import com.dashlane.session.repository.TeamspaceManagerRepository;
import com.dashlane.session.repository.UserAccountInfoRepository;
import com.dashlane.session.repository.UserCryptographyRepository;
import com.dashlane.storage.DataStorageProvider;
import com.dashlane.update.AppUpdateInstaller;
import com.dashlane.useractivity.AggregateUserActivity;
import com.dashlane.useractivity.LogSenderService;
import com.dashlane.useractivity.RacletteLogger;
import com.dashlane.useractivity.log.usage.UsageLogRepository;
import com.dashlane.util.EmulatorDetector;
import com.dashlane.util.StaticTimerUtil;
import com.dashlane.util.log.DashlaneLaunchDetector;
import com.dashlane.util.log.DeviceInformationLog;
import com.dashlane.util.notification.NotificationHelper;
import com.dashlane.util.strictmode.StrictModeUtil;
import com.google.firebase.FirebaseApp;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;

import androidx.annotation.VisibleForTesting;
import kotlin.io.FilesKt;



public class DashlaneApplicationObserver implements DashlaneApplication.ApplicationObserver {

    @Override
    public void onCreate(DashlaneApplication application) {
        StrictModeUtil.init();

        FirebaseApp.initializeApp(application);
        StaticTimerUtil.setAppAbsoluteStart(System.currentTimeMillis());
        SingletonProvider.init(application);
        UpdateManager.setFirstRunVersionCode(application);
        SingletonProvider.getComponent().getDeviceInfoUpdater().refreshIfNeeded();

        
        SQLiteDatabase.loadLibs(application);

        SingletonProvider.getThreadHelper().init();
        SingletonProvider.getDaDaDa().listen(application);
        SingletonProvider.getGlobalActivityLifecycleListener().register(application);
        SingletonProvider.getAnnouncementCenter().start(application);

        if (!DeveloperUtilities.systemIsInDebug(application.getApplicationContext())) {
            application.registerActivityLifecycleCallbacks(new AdjustActivityCallback());
        }

        initialize(application);
        DashlaneHelper.setStartTime(System.currentTimeMillis());
        EmulatorDetector.doEmulatorCheck();

        SingletonProvider.getBillingManager().connect();
        final NotificationHelper notificationHelper = SingletonProvider.getNotificationHelper();
        notificationHelper.initChannels();

        SingletonProvider.getBrazeWrapper().configureBrazeNotificationFactory();

        new DeviceInformationLog().sendIfNecessary(application);

        SingletonProvider.getComponent().getDarkThemeHelper().onApplicationCreate(application);

        SingletonProvider.getComponent().getUserSupportFileLoggerApplicationCreated().onApplicationCreated(application);

        SingletonProvider.getComponent().getVaultReportLogger().start();

        SingletonProvider.getComponent().getAuthenticatorAppConnection().loadOtpsForBackup();
    }

    

    @Override
    public void onTerminate(DashlaneApplication application) {
        SingletonProvider.getSessionManager().detachAll();
        DataSync sync = DataSync.getInstance();
        sync.cancelNotifications();
        sync.onTerminate();
        SingletonProvider.getAppEvents().unregisterAll();
        SingletonProvider.getGlobalActivityLifecycleListener().unregister(application);
        SingletonProvider.getAnnouncementCenter().stop(application);
        SingletonProvider.getBillingManager().disconnect();
        SingletonProvider.getComponent().getVaultReportLogger().stop();
    }


    @VisibleForTesting
    protected void initialize(final Application application) {
        SingletonComponentProxy component = SingletonProvider.getComponent();
        GlobalPreferencesManager globalPreferencesManager = SingletonProvider.getGlobalPreferencesManager();
        setupSessionObservers(
                SingletonProvider.getContext(),
                SingletonProvider.getSessionManager(),
                SingletonProvider.getComponent().getUserCryptographyRepository(),
                component.getSessionCoroutineScopeRepository(),
                component.getUserAccountInfoRepository(),
                component.getAccountStatusRepository(),
                component.getLockRepository(),
                component.getTeamspaceRepository(),
                SingletonProvider.getDataSync(),
                SingletonProvider.getOfflineExperimentsReporter(),
                SingletonProvider.getAggregateUserActivity(),
                SingletonProvider.getAnnouncementCenter(),
                component.getDeviceUpdateManager(),
                SingletonProvider.getCrashReporter(),
                SingletonProvider.getFcmHelper(),
                component.getAccountRecovery(),
                component.getBySessionUsageLogRepository(),
                component.getDashlaneApi(),
                component.getUserAccountStorage(),
                globalPreferencesManager,
                SingletonProvider.getAppUpdateInstaller(),
                SingletonProvider.getUserPreferencesManager(),
                SingletonProvider.getInAppLoginManager(),
                SingletonProvider.getComponent().getCryptographyMigrationObserver(),
                SingletonProvider.getMonitorAutofillIssuesSessionStartTrigger(),
                SingletonProvider.getComponent().getDataStorageProvider(),
                component.getEndOfLife(),
                SingletonProvider.getComponent().getRacletteLogger(),
                SingletonProvider.getComponent().getAppMetaDataToLinkedAppsMigration(),
                SingletonProvider.getComponent().getRememberToLinkedAppsMigration()
        );
        BroadcastManager.removeAllBufferedIntent();

        
        
        SessionRestorerUtilsKt.startRestoreSession(
                SingletonProvider.getSessionRestorer(),
                globalPreferencesManager.getDefaultUsername()
        );

        DashlaneLaunchDetector.listenApplication(application);

        setupMarketingStuff(application, globalPreferencesManager,
                component.getWebservicesRetrofit().create(LogSenderService.class));

        SingletonProvider.getThreadHelper().runOnBackgroundThread(() -> {
            
            boolean updateNeeded = UpdateManager.shouldDoUpdate(application);
            if (updateNeeded) {
                
                removeCppDataRules(application);
            }
        });

        
        SingletonProvider.getCrashReporter().init(application);
    }

    private static void removeCppDataRules(Context context) {
        File unzipFolder = new File(context.getFilesDir(), "rules");
        if (unzipFolder.exists()) {
            FilesKt.deleteRecursively(unzipFolder);
        }
    }

    private void setupMarketingStuff(final Context context, final GlobalPreferencesManager preferencesManager,
                                     final LogSenderService service) {
        if (DeveloperUtilities.systemIsInDebug(context)) {
            return;
        }

        new Thread(() -> {
            Looper.prepare();
            InstallReporter.recordInstallLogToServer(context, preferencesManager, service);
        }).start();

        SingletonProvider.getAdjustWrapper().initIfNeeded(context);
    }

    private void setupSessionObservers(
            Context context,
            SessionManager sessionManager,
            UserCryptographyRepository userCryptographyRepository,
            SessionCoroutineScopeRepository sessionCoroutineScopeRepository,
            UserAccountInfoRepository userAccountInfoRepository,
            AccountStatusRepository accountStatusRepository,
            LockRepository lockRepository,
            TeamspaceManagerRepository teamspaceManagerRepository,
            DataSync dataSync,
            OfflineExperimentReporter offlineExperimentReporter,
            AggregateUserActivity aggregateUserActivity,
            AnnouncementCenter announcementCenter,
            DeviceUpdateManager deviceUpdateManager,
            CrashReporter crashReporter,
            FcmHelper fcmHelper,
            AccountRecovery accountRecovery,
            BySessionRepository<UsageLogRepository> bySessionUsageLogRepository,
            DashlaneApi dashlaneApi,
            UserAccountStorage userAccountStorage,
            GlobalPreferencesManager globalPreferencesManager,
            AppUpdateInstaller appUpdateInstaller,
            UserPreferencesManager userPreferencesManager,
            InAppLoginManager inAppLoginManager,
            CryptographyMigrationObserver cryptographyMigrationObserver,
            MonitorAutofillIssuesSessionStartTrigger monitorAutofillIssuesSessionStartTrigger,
            DataStorageProvider dataStorageProvider,
            EndOfLifeObserver endOfLifeObserver,
            RacletteLogger racletteLogger,
            AppMetaDataToLinkedAppsMigration appMetaDataToLinkedAppsMigration,
            RememberToLinkedAppsMigration rememberToLinkedAppsMigration
    ) {
        sessionManager.attach(sessionCoroutineScopeRepository);
        sessionManager.attach(lockRepository);
        sessionManager.attach(teamspaceManagerRepository);
        sessionManager.attach(new AnnouncementCenterObserver(announcementCenter));
        sessionManager.attach(new SyncObserver(dataSync));
        sessionManager.attach(accountStatusRepository);
        sessionManager.attach(userAccountInfoRepository);
        sessionManager.attach(cryptographyMigrationObserver);
        sessionManager.attach(new BroadcastManagerObserver());
        sessionManager.attach(new OfflineExperimentObserver(offlineExperimentReporter));
        sessionManager.attach(new LogsObserver(aggregateUserActivity, bySessionUsageLogRepository));
        sessionManager
                .attach(new DeviceUpdateManagerObserver(context, deviceUpdateManager, crashReporter,
                        accountRecovery, inAppLoginManager, dataStorageProvider));
        sessionManager.attach(new FcmRegistrationObserver(fcmHelper));
        sessionManager.attach(appUpdateInstaller);
        sessionManager.attach(new BackupTokenObserver(dashlaneApi, userAccountStorage, globalPreferencesManager,
                new BackupManager(context), userCryptographyRepository));
        sessionManager.attach(new SystemUpdateObserver(userPreferencesManager));
        sessionManager.attach(monitorAutofillIssuesSessionStartTrigger);
        sessionManager.attach(new UserSettingsLogObserver(context));
        sessionManager.attach(endOfLifeObserver);
        sessionManager.attach(new RacletteLoggerObserver(racletteLogger));
        sessionManager.attach(new LinkedAppsMigrationObserver(appMetaDataToLinkedAppsMigration, rememberToLinkedAppsMigration));
    }
}
