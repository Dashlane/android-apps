package com.dashlane.async.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adjust.sdk.AdjustReferrerReceiver;
import com.dashlane.analytics.install.InstallTrackingManager;
import com.dashlane.async.BroadcastManager;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.debug.DeveloperUtilities;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.util.Constants;

import java.util.Set;

public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context receiverContext, final Intent intent) {
        
        SingletonProvider.getThreadHelper().runOnBackgroundThread(() -> {

            
            final Context context = SingletonProvider.getContext();
            GlobalPreferencesManager preferencesManager = SingletonProvider.getGlobalPreferencesManager();
            InstallTrackingManager installTracker = new InstallTrackingManager();
            installTracker.installEvent(context, intent);
            
            if (DeveloperUtilities.systemIsInDebug(context)) {
                return;
            }

            BroadcastManager.removeAllBufferedIntent();

            if (!DeveloperUtilities.systemIsInDebug(context)) {
                sendAdjustEvent(context, intent);
            }
            if (intent == null) {
                return;
            }
            if (intent.getExtras() != null) {
                Set<String> keys = intent.getExtras().keySet();
                StringBuilder result = new StringBuilder();
                for (String string : keys) {
                    result.append(string)
                          .append("=")
                          .append(intent.getStringExtra(string))
                          .append("&");
                }
            }

            preferencesManager.putBoolean(Constants.MARKETING.SHOULD_SEND_REPORTS, true);

            if (intent.hasExtra("referrer")) {
                preferencesManager.putString(Constants.MARKETING.REFFERAL_STRING,
                                             intent.getExtras().getString("referrer"));
            }
        });

    }

    private void sendAdjustEvent(Context context, Intent intent) {
        SingletonProvider.getAdjustWrapper().initIfNeeded(context);
        new AdjustReferrerReceiver().onReceive(context, intent);
    }
}
