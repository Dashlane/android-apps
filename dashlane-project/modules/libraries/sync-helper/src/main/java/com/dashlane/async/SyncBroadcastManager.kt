package com.dashlane.async

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dashlane.events.BroadcastConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncBroadcastManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var lastPasswordBroadcastIntent: Intent? = null

    fun popPasswordBroadcast(): Intent? {
        val buffIntent = lastPasswordBroadcastIntent
        lastPasswordBroadcastIntent = null

        return buffIntent
    }

    fun sendPasswordErrorBroadcast() {
        try {
            val passwordIntent = Intent(BroadcastConstants.PASSWORD_SUCCESS_BROADCAST)
            passwordIntent.putExtra(BroadcastConstants.SUCCESS_EXTRA, false)
            LocalBroadcastManager.getInstance(context).sendBroadcast(passwordIntent)
            lastPasswordBroadcastIntent = passwordIntent
        } catch (e: Exception) {
            
            logBroadcastError(e)
        }
    }

    fun removePasswordBroadcastIntent() {
        lastPasswordBroadcastIntent = null
    }

    private fun logBroadcastError(e: Exception) {
    }
}