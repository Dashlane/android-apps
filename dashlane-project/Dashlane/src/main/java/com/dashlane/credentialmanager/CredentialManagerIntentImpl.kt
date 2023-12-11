package com.dashlane.credentialmanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dashlane.login.LoginActivity
import com.dashlane.navigation.NavigationConstants
import com.dashlane.security.DashlaneIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CredentialManagerIntentImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CredentialManagerIntent {

    override fun loginToDashlaneIntent(): PendingIntent {
        val intent = DashlaneIntent.newInstance(context, LoginActivity::class.java).apply {
            putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}