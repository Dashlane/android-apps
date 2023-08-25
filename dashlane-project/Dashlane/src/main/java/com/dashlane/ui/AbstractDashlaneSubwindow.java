package com.dashlane.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.dashlane.R;
import com.dashlane.ui.utils.DashlaneSubwindowPositionUtils;
import com.dashlane.util.notification.NotificationHelper;
import com.dashlane.util.notification.NotificationUtilsKt;

import androidx.core.app.NotificationCompat;
import wei.mark.standout.StandOutWindow;

public abstract class AbstractDashlaneSubwindow extends StandOutWindow {

    public static final String DATA_POSX = "data_posx";
    public static final String DATA_POSY = "data_posy";
    public static final String DATA_OPTIM_AREA_X = "data_optim_area_x";
    public static final String DATA_OPTIM_AREA_Y = "data_optim_area_y";
    public static final String DATA_BUBBLE_POSITION_X = "data_bubble_position_x";
    public static final String DATA_BUBBLE_POSITION_Y = "data_bubble_position_y";

    public static final String DATA_SUBWINDOW_DIMENSIONS = "data_subwindow_dimensions";

    protected ImageView mArrowBottom;
    protected ImageView mArrowTop;
    protected ImageView mArrowLeft;
    protected ImageView mArrowRight;

    @Override
    public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> fromCls, int
            fromId) {
        super.onReceiveData(id, requestCode, data, fromCls, fromId);
        if (getWindow(id) != null) {

            showArrowFromOptimalPositionY(data.getInt(DATA_OPTIM_AREA_Y), data.getInt(DATA_OPTIM_AREA_X));
            setArrowMarginFromOptimalPosition(data.getIntArray(DATA_SUBWINDOW_DIMENSIONS), data.getInt
                    (DATA_OPTIM_AREA_X), data.getInt(DATA_BUBBLE_POSITION_X));

            getWindow(id).edit().setPosition(
                    data.getInt(DATA_POSX),
                    data.getInt(DATA_POSY)
                                            ).commit();
        }
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

    private void showArrowFromOptimalPositionY(int optimalY, int optimalX) {
        switch (optimalY) {
            case DashlaneSubwindowPositionUtils.OPTIMAL_POSITION_TOP:
                setVisibility(mArrowTop, View.GONE);
                setVisibility(mArrowRight, View.GONE);
                setVisibility(mArrowBottom, View.VISIBLE);
                setVisibility(mArrowLeft, View.GONE);
                break;

            case DashlaneSubwindowPositionUtils.OPTIMAL_POSITION_BOTTOM:
                setVisibility(mArrowTop, View.VISIBLE);
                setVisibility(mArrowRight, View.GONE);
                setVisibility(mArrowBottom, View.GONE);
                setVisibility(mArrowLeft, View.GONE);

                break;
            case DashlaneSubwindowPositionUtils.OPTIMAL_POSITION_CENTER:
                if (optimalX == DashlaneSubwindowPositionUtils.OPTIMAL_POSITION_SIDE_LEFT) {
                    setVisibility(mArrowTop, View.GONE);
                    setVisibility(mArrowRight, View.VISIBLE);
                    setVisibility(mArrowBottom, View.GONE);
                    setVisibility(mArrowLeft, View.GONE);
                } else {
                    setVisibility(mArrowTop, View.GONE);
                    setVisibility(mArrowRight, View.GONE);
                    setVisibility(mArrowBottom, View.GONE);
                    setVisibility(mArrowLeft, View.VISIBLE);
                }
        }
    }

    private void setVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private void setArrowMarginFromOptimalPosition(int[] windowDimensions, int optimalX, int bubblePosX) {
        int paddingRight = 0;
        switch (optimalX) {
            case DashlaneSubwindowPositionUtils.OPTIMAL_POSITION_LEFT:
                paddingRight = getResources().getDimensionPixelSize(R.dimen.dashlane_small_bubble_half_size) -
                               getResources().getDimensionPixelSize(R.dimen.spacing_small);
                break;
            case DashlaneSubwindowPositionUtils.OPTIMAL_POSITION_RIGHT:
                paddingRight = windowDimensions[0] - (getResources().getDimensionPixelSize(R.dimen
                                                                                                   .dashlane_small_bubble_width) /
                                                      2)
                               - (getResources().getDimensionPixelSize(R.dimen.dashlane_arrow_padding_corrector))
                               - getResources().getDimensionPixelSize(R.dimen.spacing_small)
                ;
                break;
            case DashlaneSubwindowPositionUtils.OPTIMAL_POSITION_CENTER:
                paddingRight = windowDimensions[0] - bubblePosX - (getResources().getDimensionPixelSize(R.dimen
                                                                                                                .dashlane_small_bubble_width) /
                                                                   2)
                               - (getResources().getDimensionPixelSize(R.dimen.dashlane_arrow_padding_corrector))
                               - getResources().getDimensionPixelSize(R.dimen.spacing_small)
                ;
                break;
        }
        if (mArrowBottom != null) {
            mArrowBottom.setPadding(0, 0, paddingRight, 0);
        }
        if (mArrowTop != null) {
            mArrowTop.setPadding(0, 0, paddingRight, 0);
        }
    }
}
