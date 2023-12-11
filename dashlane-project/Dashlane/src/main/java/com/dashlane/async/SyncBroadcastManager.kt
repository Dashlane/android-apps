package com.dashlane.async

import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dashlane.events.AppEvents
import com.dashlane.events.SyncFinishedEvent
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.security.DashlaneIntent
import com.dashlane.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncBroadcastManager @Inject constructor(
    private val appEvents: AppEvents,
    @ApplicationContext private val context: Context
) {

    private var lastPasswordBroadcastIntent: Intent? = null

    fun sendSyncFinishedBroadcast(origin: Trigger) {
        onSyncFinished(SyncFinishedEvent.State.SUCCESS, origin)
    }

    fun sendSyncFailedBroadcast(origin: Trigger) {
        onSyncFinished(SyncFinishedEvent.State.ERROR, origin)
    }

    fun sendOfflineSyncFailedBroadcast(origin: Trigger) {
        onSyncFinished(SyncFinishedEvent.State.OFFLINE, origin)
    }

    fun popPasswordBroadcast(): Intent? {
        val buffIntent = lastPasswordBroadcastIntent
        lastPasswordBroadcastIntent = null

        return buffIntent
    }

    fun sendPasswordErrorBroadcast() {
        try {
            val passwordIntent = Intent(Constants.BROADCASTS.PASSWORD_SUCCESS_BROADCAST)
            passwordIntent.putExtra(Constants.BROADCASTS.SUCCESS_EXTRA, false)
            LocalBroadcastManager.getInstance(context).sendBroadcast(passwordIntent)
            lastPasswordBroadcastIntent = passwordIntent
        } catch (e: Exception) {
            
            logBroadcastError(e)
        }
    }

    fun sendSyncShowProgressBroadcast(show: Boolean) {
        try {
            val syncBroadcast = DashlaneIntent.newInstance(Constants.BROADCASTS.SYNC_PROGRESS_BROADCAST)
            syncBroadcast.putExtra(Constants.BROADCASTS.SYNC_PROGRESS_BROADCAST_SHOW_PROGRESS, show)
            LocalBroadcastManager.getInstance(context).sendBroadcast(syncBroadcast)
        } catch (e: Exception) {
            
            logBroadcastError(e)
        }
    }

    fun removePasswordBroadcastIntent() {
        lastPasswordBroadcastIntent = null
    }

    private fun logBroadcastError(e: Exception) {
        warn("", "", e)
    }

    @WorkerThread
    private fun onSyncFinished(state: SyncFinishedEvent.State, origin: Trigger) {
        try {
            notifySyncFinished(state, origin)
        } catch (e: Exception) {
            
            logBroadcastError(e)
        }
    }

    private fun notifySyncFinished(state: SyncFinishedEvent.State, origin: Trigger) {
        val success = state === SyncFinishedEvent.State.SUCCESS
        val syncFinishIntent = Intent(Constants.BROADCASTS.SYNCFINISHED_BROADCAST)
        syncFinishIntent.putExtra(Constants.BROADCASTS.SUCCESS_EXTRA, success)
        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(syncFinishIntent)
        val trigger: SyncFinishedEvent.Trigger = if (Trigger.MANUAL == origin) {
            SyncFinishedEvent.Trigger.BY_USER
        } else {
            SyncFinishedEvent.Trigger.OTHER
        }
        appEvents.post(SyncFinishedEvent(state, trigger))
    }
}
