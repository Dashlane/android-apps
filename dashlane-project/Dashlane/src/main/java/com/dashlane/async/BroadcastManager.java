package com.dashlane.async;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.WorkerThread;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dashlane.core.DataSync;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.events.SyncFinishedEvent;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.security.DashlaneIntent;
import com.dashlane.useractivity.log.usage.UsageLogCode134;
import com.dashlane.util.Constants;

import java.util.HashMap;
import java.util.Map;



public class BroadcastManager {

    private static Map<Broadcasts, Intent> bufferedIntents = new HashMap<>();

    public static Intent getLastBroadcast(Broadcasts type) {
        Intent buffIntent = bufferedIntents.get(type);
        try {
            bufferedIntents.remove(type);
        } catch (Exception e) {
            
        }
        return buffIntent;
    }

    

    public static void sendPasswordErrorBroadcast() {
        try {
            Intent passwordIntent = new Intent(Constants.BROADCASTS.PASSWORD_SUCCESS_BROADCAST);
            passwordIntent.putExtra(Constants.BROADCASTS.SUCCESS_EXTRA, false);
            Context context = SingletonProvider.getContext();
            LocalBroadcastManager.getInstance(context).sendBroadcast(passwordIntent);
            bufferedIntents.put(Broadcasts.PasswordBroadcast, passwordIntent);
        } catch (Exception e) {
            
            logBroadcastError(e);
        }
    }

    private static void logBroadcastError(Exception e) {
        ExceptionLog.v(e);
    }

    public static void sendSyncFinishedBroadcast(UsageLogCode134.Origin origin) {
        onSyncFinished(SyncFinishedEvent.State.SUCCESS, origin);
    }

    public static void sendSyncFailedBroadcast(UsageLogCode134.Origin origin) {
        onSyncFinished(SyncFinishedEvent.State.ERROR, origin);
    }

    public static void sendOfflineSyncFailedBroadcast(UsageLogCode134.Origin origin) {
        onSyncFinished(SyncFinishedEvent.State.OFFLINE, origin);
    }

    @WorkerThread
    private static void onSyncFinished(SyncFinishedEvent.State state, UsageLogCode134.Origin origin) {
        try {
            boolean success = state == SyncFinishedEvent.State.SUCCESS;
            DataSync.getInstance().onGlobalSyncFinished(success);
            notifySyncFinished(state, origin);
        } catch (Exception e) {
            
            logBroadcastError(e);
        }
    }

    private static void notifySyncFinished(SyncFinishedEvent.State state, UsageLogCode134.Origin origin) {
        boolean success = state == SyncFinishedEvent.State.SUCCESS;
        Intent syncFinishIntent = new Intent(Constants.BROADCASTS.SYNCFINISHED_BROADCAST);
        syncFinishIntent.putExtra(Constants.BROADCASTS.SUCCESS_EXTRA, success);
        LocalBroadcastManager.getInstance(SingletonProvider.getContext())
                             .sendBroadcast(syncFinishIntent);
        SyncFinishedEvent.Trigger trigger;
        if (UsageLogCode134.Origin.MANUAL.equals(origin)) {
            trigger = SyncFinishedEvent.Trigger.BY_USER;
        } else {
            trigger = SyncFinishedEvent.Trigger.OTHER;
        }
        SingletonProvider.getAppEvents().post(new SyncFinishedEvent(state, trigger));
    }

    public static void sendSyncShowProgressBroadcast(boolean show) {
        try {
            Intent syncBroadcast = DashlaneIntent.newInstance(Constants.BROADCASTS.SYNC_PROGRESS_BROADCAST);
            syncBroadcast.putExtra(Constants.BROADCASTS.SYNC_PROGRESS_BROADCAST_SHOW_PROGRESS, show);
            LocalBroadcastManager.getInstance(SingletonProvider.getContext())
                                 .sendBroadcast(syncBroadcast);
        } catch (Exception e) {
            
            logBroadcastError(e);
        }
    }

    public static void removeBufferedIntentFor(Broadcasts type) {
        if (bufferedIntents != null) {
            bufferedIntents.remove(type);
        }
    }

    public static void removeAllBufferedIntent() {
        if (bufferedIntents != null)
            bufferedIntents.clear();
    }

    public enum Broadcasts {
        PasswordBroadcast
    }
}
