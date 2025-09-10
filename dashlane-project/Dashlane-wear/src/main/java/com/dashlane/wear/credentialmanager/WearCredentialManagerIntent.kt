package com.dashlane.wear.credentialmanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dashlane.credentialmanager.CredentialManagerIntent
import com.dashlane.navigation.NavigationConstants
import com.dashlane.security.DashlaneIntent
import com.dashlane.wear.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WearCredentialManagerIntent @Inject constructor(
    @ApplicationContext private val context: Context,
) : CredentialManagerIntent {
    override fun loginToDashlaneIntent(): PendingIntent {
        val intent = DashlaneIntent.newInstance(context, MainActivity::class.java).apply {
            putExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, true)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}