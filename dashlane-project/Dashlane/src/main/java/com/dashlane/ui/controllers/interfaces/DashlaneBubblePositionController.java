package com.dashlane.ui.controllers.interfaces;

import android.content.Context;
import android.os.Bundle;

import wei.mark.standout.ui.Window;



public interface DashlaneBubblePositionController {

    void animateWindowToPosition(Context context, final Window window, Bundle positionBundle);

    void animatePosition(Window window, int fromX, int fromY, int toX, int toY);
}
