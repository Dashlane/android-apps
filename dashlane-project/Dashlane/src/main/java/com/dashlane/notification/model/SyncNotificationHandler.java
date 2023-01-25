package com.dashlane.notification.model;

import android.content.Context;

import com.dashlane.core.DataSync;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.notification.FcmMessage;
import com.dashlane.session.Session;
import com.dashlane.useractivity.log.usage.UsageLogCode134;

import org.json.JSONException;
import org.json.JSONObject;

public class SyncNotificationHandler extends AbstractNotificationHandler {

    private static final int NOTIFICATION_ID = SyncNotificationHandler.class.hashCode();
    private static final String JSON_KEY_DEVICE_ID = "deviceId";
    private String mDeviceId;

    public SyncNotificationHandler(Context context, FcmMessage message) {
        super(context, message);
        parseMessage();
        setNotificationId(NOTIFICATION_ID);
    }

    @Override
    public void handlePushNotification() {
        syncIfUserLoggedIn();
    }

    @Override
    protected void parseMessage() {
        super.parseMessage();
        String gcmData = getFcmMessage().getData();
        try {
            JSONObject jsonFormatedData = new JSONObject(gcmData);

            if (jsonFormatedData.has(JSON_KEY_DEVICE_ID)) {
                mDeviceId = jsonFormatedData.getString(JSON_KEY_DEVICE_ID);
            }
        } catch (JSONException e) {
            ExceptionLog.v(e);
        }
    }

    private void syncIfUserLoggedIn() {
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session == null) {
            return;
        }
        if ((mDeviceId == null ||
             !mDeviceId.equals(session.getDeviceId())) &&
             getRecipientEmail().equals(session.getUserId())) {
            DataSync.sync(UsageLogCode134.Origin.PUSH);
        }
    }
}