package com.dashlane.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.dashlane.R;
import com.dashlane.async.BroadcastManager;
import com.dashlane.core.sync.DataSyncHelper;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.debug.DaDaDa;
import com.dashlane.debug.DeveloperUtilities;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.logger.utils.LogsSender;
import com.dashlane.managers.PerfLogManager;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.session.Session;
import com.dashlane.sync.SyncComponent;
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationUtilsKt;
import com.dashlane.teamspaces.manager.SpaceDeletedNotifier;
import com.dashlane.useractivity.log.usage.UsageLogCode134;
import com.dashlane.util.AppSync;
import com.dashlane.util.Constants;
import com.dashlane.util.Network;
import com.dashlane.util.StringUtils;
import com.dashlane.util.TimerCounter;
import com.dashlane.util.notification.DashlaneNotificationBuilder;
import com.dashlane.util.notification.NotificationHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;



@Singleton
public class DataSync implements AppSync {
    private static final String LOG_TAG = "datasync";
    private static long lastSync = 0L;
    private static long startTime;
    private static final int SYNC_NOTIFICATION_ID = 0;

    private final DaDaDa mDaDaDa;
    private final DataSyncHelper mSyncHelper;
    private final NotificationManagerCompat notificationManager;
    private final Notification syncNotification;
    private final ExecutorService mService;
    private final Provider<SpaceDeletedNotifier> mSpaceDeletedNotifierProvider;

    private boolean mGlobalSyncRunning;
    private boolean mSyncBlocked = false;

    @Inject
    public DataSync(DataSyncHelper syncHelper, Provider<SpaceDeletedNotifier> spaceDeletedNotifierProvider,
                    DaDaDa daDaDa) {
        mSyncHelper = syncHelper;
        mSpaceDeletedNotifierProvider = spaceDeletedNotifierProvider;
        mDaDaDa = daDaDa;

        Context context = SingletonProvider.getContext();
        
        notificationManager = NotificationManagerCompat.from(context);

        int icon = android.R.drawable.stat_notify_sync;
        CharSequence tickerText = context.getString(R.string.dashlane_sync_in_progress);
        CharSequence bodyText = context.getString(R.string.your_dashlane_account_is_beeing_synchronized);

        syncNotification = new DashlaneNotificationBuilder(context)
                .setContentTitle(tickerText)
                .setContentText(bodyText)
                .setSmallIcon(icon)
                .setChannel(NotificationHelper.Channel.PASSIVE)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .build(); 


        Intent notificationIntent = new Intent();
        syncNotification.contentIntent =
                PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        mService = Executors.newFixedThreadPool(1);
    }

    public static DataSync getInstance() {
        return SingletonProvider.getDataSync();
    }

    public DataSyncHelper getSyncHelper() {
        return mSyncHelper;
    }

    public SyncComponent getSyncComponent() {
        return mSyncHelper.getSyncComponent();
    }

    public static void sync(UsageLogCode134.Origin origin) {
        Context context = SingletonProvider.getContext();
        if (Network.isOn(context)) {
            getInstance().uploadAll(context, origin);
            LogsSender.flushLogs();
        } else {
            BroadcastManager.sendOfflineSyncFailedBroadcast(origin);
        }
    }

    

    public void maySync() {
        if (startTime == 0) {
            sync(UsageLogCode134.Origin.WAKE);
            return;
        }
        if (System.currentTimeMillis() - startTime > getSyncRefreshInterval()) {
            sync(UsageLogCode134.Origin.WAKE);
        }
    }

    public static int getLastSyncDuration() {
        return (int) lastSync;
    }

    @Override
    public void sync() {
        sync(UsageLogCode134.Origin.SAVE);
    }

    
    @SuppressWarnings("squid:S2696")
    public void onGlobalSyncFinished(boolean success) {
        lastSync = ((System.currentTimeMillis()) - startTime);
        BroadcastManager.sendSyncShowProgressBroadcast(false);
        SingletonProvider.getUserPreferencesManager()
                         .putLong(ConstantsPrefs.LAST_BACKUPSYNC_TIMESTAMP, System.currentTimeMillis());

        LogsSender.flushLogs();

        
        if (success) {
            SingletonProvider.getBreachManager().refreshIfNecessary(false);
        }
    }

    private static void syncStart() {
        startTime = System.currentTimeMillis();
        BroadcastManager.sendSyncShowProgressBroadcast(true);
    }

    public void cancelNotifications() {
        if (DeveloperUtilities.systemIsInDebug(SingletonProvider.getContext())) {
            notificationManager.cancel(SYNC_NOTIFICATION_ID);
        }
    }

    private void uploadAll(final Context context, UsageLogCode134.Origin origin) {
        if ((isSyncRunning() || mSyncBlocked)) {
            return;
        }
        mService.execute(getSyncRunnable(context, origin));
    }

    public void stopSync() {
        
    }

    public void markSyncAllowed() {
        mSyncBlocked = false;
    }

    public void markSyncNotAllowed() {
        mSyncBlocked = true;
    }

    public void onTerminate() {
        mService.shutdown();
    }

    private Runnable getSyncRunnable(final Context context, UsageLogCode134.Origin origin) {
        return new SyncRunnable(context, origin);
    }

    private class SyncRunnable implements Runnable {

        private final Context mContext;

        private final UsageLogCode134.Origin mOrigin;

        SyncRunnable(Context context, UsageLogCode134.Origin origin) {
            mContext = context;
            mOrigin = origin;
        }

        @Override
        public void run() {
            if (Network.isOn(mContext)) {
                showSyncNotification();
                runSync();
                hideSyncNotification();
            } else {
                BroadcastManager.sendOfflineSyncFailedBroadcast(mOrigin);
            }
        }

        private void runSync() {
            PerfLogManager.getInstance().sendPerfLog(PerfLogManager.PerfLogLocations.preSync.name());

            setGlobalSyncRunning(true);
            syncStart();
            TimerCounter syncTimer = new TimerCounter();
            syncTimer.start();

            Session session = SingletonProvider.getSessionManager().getSession();
            try {
                String username = session.getUserId();
                String uki = session.getUki();

                
                if (StringUtils.isSemanticallyNull(uki)) {
                    throw new NullPointerException("uki is null");
                }

                mSyncHelper.runSync(session, mOrigin);

                TeamspaceForceCategorizationUtilsKt
                        .executeSyncBlocking(SingletonProvider.getComponent().getTeamspaceForceCategorizationManager());
                mSpaceDeletedNotifierProvider.get().sendIfNeeded(username, uki);

                syncTimer.stop();

                PerfLogManager.getInstance().sendSyncDuration(syncTimer.getDurationMs());

            } catch (Exception e) {
                ExceptionLog.v(e);
                if (DeveloperUtilities.systemIsInDebug(mContext)) {
                    notificationManager.cancel(SYNC_NOTIFICATION_ID);
                }
            } finally {
                setGlobalSyncRunning(false);
            }
            PerfLogManager.getInstance().sendPerfLog(PerfLogManager.PerfLogLocations.postSync.name());
        }

        private void showSyncNotification() {
            try {
                if (DeveloperUtilities.systemIsInDebug(mContext)) {
                    notificationManager.notify(SYNC_NOTIFICATION_ID, syncNotification);
                }
            } catch (Exception e) {
                
            }
        }

        private void hideSyncNotification() {
            if (DeveloperUtilities.systemIsInDebug(mContext)) {
                notificationManager.cancel(SYNC_NOTIFICATION_ID);
            }
        }
    }

    private void setGlobalSyncRunning(boolean running) {
        mGlobalSyncRunning = running;
    }

    public boolean isSyncRunning() {
        return mGlobalSyncRunning;
    }

    private long getSyncRefreshInterval() {
        return mDaDaDa.getSyncRefreshInterval() != null ?
               mDaDaDa.getSyncRefreshInterval() : Constants.SYNC_REFRESH_INTERVAL;
    }
}