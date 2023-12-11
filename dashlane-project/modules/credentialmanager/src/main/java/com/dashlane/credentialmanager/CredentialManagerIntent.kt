package com.dashlane.credentialmanager

import android.app.PendingIntent

interface CredentialManagerIntent {
    fun loginToDashlaneIntent(): PendingIntent
}