package com.dashlane.authentication.accountsmanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class DashlaneAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new DashlaneAuthenticator(this).getIBinder();
    }
}