package com.dashlane.async.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dashlane.dagger.singleton.SingletonProvider;

public class GCMAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("notificationId")) {
            int id = intent.getIntExtra("notificationId", -1);
            if (id != -1) {
                SingletonProvider.getFcmHelper().clearNotification(context, id);
                return;
            }
        }
        SingletonProvider.getFcmHelper().clearAllNotification(context);
    }
}