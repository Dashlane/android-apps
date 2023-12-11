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

class BroadcastManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appEvents: AppEvents
) {
    private val bufferedIntents: MutableMap<Broadcasts, Intent> = mutableMapOf()
    fun getLastBroadcast(type: Broadcasts): Intent? {
        val buffIntent = bufferedIntents[type]
        runCatching { bufferedIntents.remove(type) }
        return buffIntent
    }

    fun sendPasswordErrorBroadcast() {
        try {
            val passwordIntent = Intent(Constants.BROADCASTS.PASSWORD_SUCCESS_BROADCAST)
            passwordIntent.putExtra(Constants.BROADCASTS.SUCCESS_EXTRA, false)
            LocalBroadcastManager.getInstance(context).sendBroadcast(passwordIntent)
            bufferedIntents[Broadcasts.PasswordBroadcast] = passwordIntent
        } catch (e: Exception) {
            
            logBroadcastError(e)
        }
    }

    private fun logBroadcastError(e: Exception) {
        warn("", "", e)
    }

    fun sendSyncFinishedBroadcast(origin: Trigger) {
        onSyncFinished(SyncFinishedEvent.State.SUCCESS, origin)
    }

    fun sendSyncFailedBroadcast(origin: Trigger) {
        onSyncFinished(SyncFinishedEvent.State.ERROR, origin)
    }

    fun sendOfflineSyncFailedBroadcast(origin: Trigger) {
        onSyncFinished(SyncFinishedEvent.State.OFFLINE, origin)
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
        context.sendBroadcast(syncFinishIntent)
        val trigger: SyncFinishedEvent.Trigger = if (Trigger.MANUAL == origin) {
            SyncFinishedEvent.Trigger.BY_USER
        } else {
            SyncFinishedEvent.Trigger.OTHER
        }
        appEvents.post(SyncFinishedEvent(state, trigger))
    }

    fun sendSyncShowProgressBroadcast(show: Boolean) {
        try {
            val syncBroadcast =
                DashlaneIntent.newInstance(Constants.BROADCASTS.SYNC_PROGRESS_BROADCAST)
            syncBroadcast.putExtra(Constants.BROADCASTS.SYNC_PROGRESS_BROADCAST_SHOW_PROGRESS, show)
            context.sendBroadcast(syncBroadcast)
        } catch (e: Exception) {
            
            logBroadcastError(e)
        }
    }

    fun removeBufferedIntentFor(type: Broadcasts) {
        bufferedIntents.remove(type)
    }

    fun removeAllBufferedIntent() {
        bufferedIntents.clear()
    }

    enum class Broadcasts {
        PasswordBroadcast
    }
}