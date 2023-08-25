package com.dashlane.notification.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.notification.FcmMessage;
import com.dashlane.security.DashlaneIntent;
import com.dashlane.util.Constants;
import com.dashlane.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.NotificationManagerCompat;

public abstract class AbstractNotificationHandler {

    private static final String JSON_KEY_TTL = "ttl";

    private String mRecipientEmail;
    private long mTimeToLive = -1;
    private int mNotificationId;

    private Context mContext;
    private FcmMessage mFcmMessage;

    protected AbstractNotificationHandler(Context context, FcmMessage fcmMessage) {
        mContext = context;
        mFcmMessage = fcmMessage;
    }

    public void clearNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(mNotificationId);
    }

    public abstract void handlePushNotification();

    public Context getContext() {
        return mContext;
    }

    public FcmMessage getFcmMessage() {
        return mFcmMessage;
    }

    protected String getRecipientEmail() {
        return mRecipientEmail;
    }

    protected boolean isForLastLoggedInUser() {
        String lastLoggedInUser = SingletonProvider.getGlobalPreferencesManager().getLastLoggedInUser();
        return lastLoggedInUser != null && lastLoggedInUser.equals(mRecipientEmail);
    }

    protected int getNotificationId() {
        return mNotificationId;
    }

    protected void setNotificationId(int id) {
        mNotificationId = id;
    }

    protected long getTTL() {
        return mTimeToLive;
    }

    protected void parseMessage() {
        String gcmData = mFcmMessage.getData();
        try {
            JSONObject jsonFormatedData = new JSONObject(gcmData);
            mRecipientEmail = mFcmMessage.getLogin();
            if (jsonFormatedData.has(JSON_KEY_TTL)) {
                mTimeToLive = jsonFormatedData.getLong(JSON_KEY_TTL);
            }
        } catch (JSONException e) {
        }
    }

    protected boolean hasTTL() {
        return mTimeToLive >= 0;
    }

    protected void setUpCancelAlarm(Context context) {
        if (hasTTL()) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = DashlaneIntent.newInstance(Constants.GCM.CLEAR_GCM_NOTIFICATION);
            i.putExtra("notificationId", mNotificationId);
            PendingIntent intentExecuted = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + mTimeToLive,
                             intentExecuted);
        }
    }

    protected boolean hasRecipient() {
        return StringUtils.isNotSemanticallyNull(mRecipientEmail);
    }
}
