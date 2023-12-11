package com.dashlane.ui;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;

import com.dashlane.R;
import com.dashlane.lock.LockHelper;
import com.dashlane.util.DeviceUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

@AndroidEntryPoint
public class DashlaneInAppLoggedOut extends AbstractDashlaneSubwindow implements View.OnClickListener {

    @Inject
    LockHelper lockHelper;
    public static final int WINDOW_ID = DashlaneInAppLoggedOut.class.hashCode();
    public static final String DATA_PACKAGE_NAME = "data_package_name";

    private LayoutInflater mLayoutInflater;

    public static int[] getWindowDimensions(Context context) {
        int[] screenSize = DeviceUtils.getScreenSize(context);
        return new int[]{
                Math.min(
                        context.getResources().getDimensionPixelSize(R.dimen
                                                                             .dashlane_content_loggedout_bubble_max_width),
                        screenSize[0]
                        ),
                context.getResources().getDimensionPixelSize(R.dimen.dashlane_content_loggedout_bubble_height)
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLayoutInflater = LayoutInflater.from(this);
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
    public int getFlags(int id) {
        return super.getFlags(id)
               | StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        mLayoutInflater.inflate(R.layout.window_dashlane_bubble_logged_out, frame, true);
        mArrowTop = frame.findViewById(R.id.arrow_top);
        mArrowBottom = frame.findViewById(R.id.arrow_bottom);
        Button yesButton = frame.findViewById(R.id.log_in_with_dashlane_yes);
        Button noButton = frame.findViewById(R.id.log_in_with_dashlane_no);
        yesButton.setOnClickListener(this);
        noButton.setOnClickListener(this);
    }

    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        int[] screenSize = DeviceUtils.getScreenSize(this);
        return new StandOutLayoutParams(id,
                                        Math.min(
                                                getResources().getDimensionPixelSize(
                                                        R.dimen.dashlane_content_loggedout_bubble_max_width),
                                                screenSize[0]
                                                ),
                                        getResources().getDimensionPixelSize(
                                                R.dimen.dashlane_content_loggedout_bubble_height),
                                        StandOutLayoutParams.LEFT,
                                        StandOutLayoutParams.TOP
        );
    }

    @Override
    public Animation getShowAnimation(int id) {
        return AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft);
    }

    @Override
    public Animation getCloseAnimation(int id) {
        return AnimationUtils.loadAnimation(this, R.anim.shrink_from_bottomleft_to_topright);
    }

    @Override
    public void onClick(View v) {
        if (R.id.log_in_with_dashlane_yes == v.getId()) {
            lockHelper.logoutAndCallLoginScreenForInAppLogin(this);
            new Handler().postDelayed(() -> StandOutWindow.closeAll(DashlaneInAppLoggedOut.this, DashlaneBubble.class), 750);
        }
        StandOutWindow.closeAll(this, DashlaneInAppLoggedOut.class);
    }

    @Override
    public boolean onCloseAll() {
        sendData(DashlaneInAppLoggedOut.WINDOW_ID, DashlaneBubble.class, DashlaneBubble.WINDOW_ID, DashlaneBubble
                .REQUEST_CODE_IN_APP_LOGGED_OUT_CLOSED, null);
        return super.onCloseAll();
    }

}
