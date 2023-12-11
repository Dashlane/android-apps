package com.dashlane.async.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dashlane.notification.FcmHelper;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GCMAlarmReceiver extends BroadcastReceiver {

    @Inject
    FcmHelper fcmHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("notificationId")) {
            int id = intent.getIntExtra("notificationId", -1);
            if (id != -1) {
                fcmHelper.clearNotification(context, id);
                return;
            }
        }
        fcmHelper.clearAllNotification(context);
    }
}