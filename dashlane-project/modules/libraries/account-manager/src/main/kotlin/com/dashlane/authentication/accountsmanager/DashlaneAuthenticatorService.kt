package com.dashlane.authentication.accountsmanager

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DashlaneAuthenticatorService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return DashlaneAuthenticator(this).iBinder
    }
}