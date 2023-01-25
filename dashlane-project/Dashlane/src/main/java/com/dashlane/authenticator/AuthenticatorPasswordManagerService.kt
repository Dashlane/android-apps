package com.dashlane.authenticator

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dashlane.authenticator.ipc.PasswordManagerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthenticatorPasswordManagerService : Service() {
    @Inject
    lateinit var passwordManagerServiceStub: PasswordManagerService.Stub

    override fun onBind(intent: Intent?): IBinder? = passwordManagerServiceStub.asBinder()
}