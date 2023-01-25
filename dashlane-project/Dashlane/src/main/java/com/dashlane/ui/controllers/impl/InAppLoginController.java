package com.dashlane.ui.controllers.impl;

import android.content.Context;

import com.dashlane.ui.DashlaneBubble;
import com.dashlane.ui.InAppLoginWindow;
import com.dashlane.ui.controllers.interfaces.DashlaneBubbleController;
import com.dashlane.ui.utils.DashlaneSubwindowPositionUtils;
import com.dashlane.useractivity.log.usage.UsageLogRepository;
import com.dashlane.useractivity.log.inject.UserActivityComponent;
import com.dashlane.useractivity.log.usage.UsageLogCode96;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.ui.Window;



public class InAppLoginController implements DashlaneBubbleController {

    private boolean isWindowOpened = false;
    private int mResultCount;
    private String mPackageName;

    public InAppLoginController(int mResultCount, String packageName) {
        super();
        this.mResultCount = mResultCount;
        mPackageName = packageName;
    }

    @Override
    public boolean onBubbleClicked(Context context, int id, Window window) {
        if (isWindowOpened) {
            isWindowOpened = false;

            sendUsageLog(context, UsageLogCode96.Action.CLOSE_WEBCARD);

            StandOutWindow.closeAll(context, InAppLoginWindow.class);
        } else {
            isWindowOpened = true;
            StandOutWindow.show(context, InAppLoginWindow.class, InAppLoginWindow.WINDOW_ID);
            StandOutWindow.sendData(context, InAppLoginWindow.class, InAppLoginWindow.WINDOW_ID, 0,
                                    DashlaneSubwindowPositionUtils.getPositionBundle(context, window, InAppLoginWindow
                                            .getWindowDimensions(context, mResultCount)), DashlaneBubble.class, id
                                   );

            sendUsageLog(context, UsageLogCode96.Action.CLIC_IMPALA);
        }

        return true;
    }

    private void sendUsageLog(Context context, UsageLogCode96.Action action) {
        UsageLogRepository usageLogRepository =
                UserActivityComponent.Companion.invoke(context).getCurrentSessionUsageLogRepository();
        new InAppLoginControllerLogger(usageLogRepository)
                .log(mPackageName, action, mResultCount > 0);
    }

    @Override
    public void onBubbleMoved(Context context, int id, Window window) {
        if (isWindowOpened) {
            StandOutWindow.sendData(context, InAppLoginWindow.class, InAppLoginWindow.WINDOW_ID, 0,
                                    DashlaneSubwindowPositionUtils.getPositionBundle(context, window, InAppLoginWindow
                                            .getWindowDimensions(context, mResultCount)), DashlaneBubble.class, id
                                   );
        }
    }

    @Override
    public void onBubbleClosed(Context context) {
        if (isWindowOpened) {
            StandOutWindow.closeAll(context, InAppLoginWindow.class);
        }
    }

    public void setWindowOpened(boolean isWindowOpened) {
        this.isWindowOpened = isWindowOpened;
    }


}
