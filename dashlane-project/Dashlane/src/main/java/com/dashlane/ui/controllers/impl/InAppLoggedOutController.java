package com.dashlane.ui.controllers.impl;

import android.content.Context;
import android.os.Bundle;

import com.dashlane.ui.DashlaneBubble;
import com.dashlane.ui.DashlaneInAppLoggedOut;
import com.dashlane.ui.controllers.interfaces.DashlaneBubbleController;
import com.dashlane.ui.utils.DashlaneSubwindowPositionUtils;
import com.dashlane.useractivity.log.install.InstallLogRepository;
import com.dashlane.useractivity.log.inject.UserActivityComponent;
import com.dashlane.useractivity.log.install.InstallLogCode42;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.ui.Window;



public class InAppLoggedOutController implements DashlaneBubbleController {

    private boolean isWindowOpened = false;
    private String mPackageName;

    public InAppLoggedOutController(String mPackageName) {
        super();
        this.mPackageName = mPackageName;
    }

    @Override
    public boolean onBubbleClicked(Context context, int id, Window window) {
        if (isWindowOpened) {
            isWindowOpened = false;

            log(context, InstallLogCode42.Action.CLOSE_WEBCARD);

            StandOutWindow.closeAll(context, DashlaneInAppLoggedOut.class);
        } else {
            isWindowOpened = true;
            StandOutWindow.show(context, DashlaneInAppLoggedOut.class, DashlaneInAppLoggedOut.WINDOW_ID);
            StandOutWindow.sendData(context, DashlaneInAppLoggedOut.class, DashlaneInAppLoggedOut.WINDOW_ID, 0,
                                    DashlaneSubwindowPositionUtils
                                            .getPositionBundle(context, window, DashlaneInAppLoggedOut
                                                    .getWindowDimensions(context)), DashlaneBubble.class, id);

            Bundle packageNameForwarder = new Bundle();
            packageNameForwarder.putString(DashlaneInAppLoggedOut.DATA_PACKAGE_NAME, mPackageName);
            StandOutWindow.sendData(context, DashlaneInAppLoggedOut.class, DashlaneInAppLoggedOut.WINDOW_ID, 0,
                                    packageNameForwarder, DashlaneBubble.class, id);

            log(context, InstallLogCode42.Action.CLIC_IMPALA);
        }
        return true;
    }

    private void log(Context context, InstallLogCode42.Action action) {
        InstallLogRepository installLogRepository =
                UserActivityComponent.Companion.invoke(context).getInstallLogRepository();
        new InAppLoggedOutControllerLogger(installLogRepository).log(mPackageName, action);
    }

    @Override
    public void onBubbleMoved(Context context, int id, Window window) {
        if (isWindowOpened) {
            StandOutWindow.sendData(context, DashlaneInAppLoggedOut.class, DashlaneInAppLoggedOut.WINDOW_ID, 0,
                                    DashlaneSubwindowPositionUtils
                                            .getPositionBundle(context, window, DashlaneInAppLoggedOut
                                                    .getWindowDimensions(context)), DashlaneBubble.class, id);
        }
    }

    public void setWindowOpened(boolean isWindowOpened) {
        this.isWindowOpened = isWindowOpened;
    }

    @Override
    public void onBubbleClosed(Context context) {
        if (isWindowOpened) {
            StandOutWindow.closeAll(context, DashlaneInAppLoggedOut.class);
        }
    }
}
