package com.dashlane.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.dashlane.R;
import com.dashlane.ui.controllers.impl.DashlaneBubbleAnimatePositionController;
import com.dashlane.ui.controllers.impl.InAppLoggedOutController;
import com.dashlane.ui.controllers.impl.InAppLoginController;
import com.dashlane.ui.controllers.interfaces.DashlaneBubbleController;
import com.dashlane.ui.controllers.interfaces.DashlaneBubblePositionController;
import com.dashlane.util.notification.NotificationHelper;
import com.dashlane.util.notification.NotificationUtilsKt;

import androidx.core.app.NotificationCompat;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;



public class DashlaneBubble extends StandOutWindow {

    

    public static final int REQUEST_CODE_MOVE_TO_FIELD = 0;

    

    public static final int REQUEST_CODE_IN_APP_LOGIN_CLOSED = 1;

    

    public static final int REQUEST_CODE_IN_APP_LOGGED_OUT_CLOSED = 2;

    

    public static final int REQUEST_CODE_CHANGE_CONTROLLER = 3;

    

    public static final String DATA_FORM_FIELD_BOUND = "data_form_field_bound";

    

    public static final String DATA_CONTROLLER_TYPE = "extra_controller_type";
    public static final String DATA_ANALYSIS_RESULT_COUNT = "data_analysis_result_count";

    public static final int WINDOW_ID = DashlaneBubble.class.hashCode();
    public static final String DATA_ANALYSIS_RESULT_APP = "data_analysis_result_packagename";
    

    public static final int CONTROLLER_LOGGED_OUT = 1;
    

    public static final int CONTROLLER_IN_APP_LOGIN = 0;
    private LayoutInflater mLayoutInflater;
    private DashlaneBubbleController mController;
    private String mPackageName;

    @Override
    public void onCreate() {
        super.onCreate();
        mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getFlags(int id) {
        return super.getFlags(id)
               | StandOutFlags.FLAG_BODY_MOVE_ENABLE
               | StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE
               | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE
               | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP;
    }

    @Override
    public String getAppName() {
        return getString(R.string.dashlane_main_app_name);
    }

    @Override
    public int getAppIcon() {
        return R.drawable.ic_notification_small_icon;
    }

    @Override
    public Notification getPersistentNotification(int id) {
        return NotificationUtilsKt.getInAppLoginBubbleNotification(this,
                getPersistentNotificationTitle(id),
                getPersistentNotificationMessage(id),
                getPersistentNotificationAction(id));
    }

    @Override
    public NotificationCompat.Action getPersistentNotificationAction(int id) {
        PendingIntent closeIntent = PendingIntent.getService(getApplicationContext(), 1,
                                                             StandOutWindow
                                                                     .getCloseAllIntent(this, DashlaneBubble.class),
                                                             PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action.Builder(R.drawable.ic_up_indicator_close,
                                                     getString(R.string.inapp_login_close_btn),
                                                     closeIntent).build();
    }

    @Override
    public Notification getHiddenNotification(int id) {
        return NotificationUtilsKt.getInAppLoginBubbleNotification(this,
                getPersistentNotificationTitle(id),
                getPersistentNotificationMessage(id), null);
    }

    @Override
    public String getPersistentNotificationTitle(int id) {
        return getString(R.string.in_app_login_persistent_notification_title);
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return getString(R.string.in_app_login_persistent_notification_message);
    }

    @Override
    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseAllIntent(this, DashlaneBubble.class);
    }


    @Override
    public String getNotificationChannelId() {
        return NotificationHelper.Channel.PASSIVE.getId();
    }

    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(id,
                                        getResources().getDimensionPixelSize(R.dimen.dashlane_small_bubble_width),
                                        getResources().getDimensionPixelSize(R.dimen.dashlane_small_bubble_height),
                                        StandOutLayoutParams.RIGHT,
                                        StandOutLayoutParams.TOP
        );
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        mLayoutInflater.inflate(R.layout.window_dashlane_bubble, frame, true);
    }

    @Override
    public boolean onBringToFront(int id, Window window) {
        return mController.onBubbleClicked(this, id, window);
    }

    @Override
    public void onMove(int id, Window window, View view, MotionEvent event) {
        super.onMove(id, window, view, event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            final int fid = id;
            final Window fwindow = window;
            new Handler().postDelayed(() -> mController.onBubbleMoved(DashlaneBubble.this, fid, fwindow), 50);
        } else {
            mController.onBubbleMoved(this, id, window);
        }
    }


    @Override
    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> fromCls, int
            fromId) {
        super.onReceiveData(id, requestCode, data, fromCls, fromId);
        switch (requestCode) {
            case REQUEST_CODE_MOVE_TO_FIELD:
                DashlaneBubblePositionController controller = new DashlaneBubbleAnimatePositionController();
                controller.animateWindowToPosition(this, getWindow(id), data);
                break;
            case REQUEST_CODE_IN_APP_LOGIN_CLOSED:
                if (mController instanceof InAppLoginController) {
                    ((InAppLoginController) mController).setWindowOpened(false);
                }
                break;
            case REQUEST_CODE_IN_APP_LOGGED_OUT_CLOSED:
                if (mController instanceof InAppLoggedOutController) {
                    ((InAppLoggedOutController) mController).setWindowOpened(false);
                }
                break;
            case REQUEST_CODE_CHANGE_CONTROLLER:
                if (mController != null) {
                    mController.onBubbleClosed(this);
                }
                mController = getControllerFromData(data);
                break;
        }
    }

    @Override
    public boolean onHide(int id, Window window) {
        if (mController != null) {
            mController.onBubbleClosed(this);
        }
        return super.onHide(id, window);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Window window = getWindow(WINDOW_ID);
        if (window != null) {
            window.refreshScreenDimensions();
        }
    }

    @Override
    public boolean onCloseAll() {
        if (mController != null) {
            mController.onBubbleClosed(this);
        }
        return super.onCloseAll();
    }

    private DashlaneBubbleController getControllerFromData(Bundle data) {
        switch (data.getInt(DATA_CONTROLLER_TYPE, 0)) {
            case CONTROLLER_IN_APP_LOGIN:
                mPackageName = data.getString(DATA_ANALYSIS_RESULT_APP);
                return new InAppLoginController(data.getInt(DATA_ANALYSIS_RESULT_COUNT), mPackageName);
            case CONTROLLER_LOGGED_OUT:
                mPackageName = data.getString(DATA_ANALYSIS_RESULT_APP);
                return new InAppLoggedOutController(mPackageName);
            default:
                return null;
        }
    }
}
