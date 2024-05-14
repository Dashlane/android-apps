package com.dashlane.async.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.adjust.sdk.AdjustReferrerReceiver
import com.dashlane.analytics.install.InstallTrackingManager
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.debug.DeveloperUtilities.systemIsInDebug
import com.dashlane.logger.AdjustWrapper
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class InstallReceiver : BroadcastReceiver() {
    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var adjustWrapper: AdjustWrapper

    @Inject
    lateinit var syncBroadcastManager: SyncBroadcastManager

    @Inject
    @ApplicationCoroutineScope
    lateinit var applicationCoroutineScope: CoroutineScope

    @Inject
    @DefaultCoroutineDispatcher
    lateinit var defaultCoroutineDispatcher: CoroutineDispatcher

    @Inject
    lateinit var installTrackingManager: InstallTrackingManager

    override fun onReceive(context: Context, intent: Intent) {
        applicationCoroutineScope.launch {
            withContext(defaultCoroutineDispatcher) {
                
                installTrackingManager.installEvent(intent)

                
                if (systemIsInDebug(context)) {
                    return@withContext
                }
                syncBroadcastManager.removePasswordBroadcastIntent()
                if (!systemIsInDebug(context)) {
                    sendAdjustEvent(context, intent)
                }
            }
        }
    }

    private fun sendAdjustEvent(context: Context, intent: Intent) {
        adjustWrapper.initIfNeeded(context)
        AdjustReferrerReceiver().onReceive(context, intent)
    }
}
