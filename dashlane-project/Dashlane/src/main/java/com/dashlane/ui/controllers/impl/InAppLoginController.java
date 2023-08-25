package com.dashlane.ui.controllers.impl;

import android.content.Context;

import com.dashlane.ui.DashlaneBubble;
import com.dashlane.ui.InAppLoginWindow;
import com.dashlane.ui.controllers.interfaces.DashlaneBubbleController;
import com.dashlane.ui.utils.DashlaneSubwindowPositionUtils;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.ui.Window;

public class InAppLoginController implements DashlaneBubbleController {

    private boolean isWindowOpened = false;
    private int mResultCount;

    public InAppLoginController(int mResultCount) {
        super();
        this.mResultCount = mResultCount;
    }

    @Override
    public boolean onBubbleClicked(Context context, int id, Window window) {
        if (isWindowOpened) {
            isWindowOpened = false;
            StandOutWindow.closeAll(context, InAppLoginWindow.class);
        } else {
            isWindowOpened = true;
            StandOutWindow.show(context, InAppLoginWindow.class, InAppLoginWindow.WINDOW_ID);
            StandOutWindow.sendData(context, InAppLoginWindow.class, InAppLoginWindow.WINDOW_ID, 0,
                                    DashlaneSubwindowPositionUtils.getPositionBundle(context, window, InAppLoginWindow
                                            .getWindowDimensions(context, mResultCount)), DashlaneBubble.class, id
                                   );
        }

        return true;
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
