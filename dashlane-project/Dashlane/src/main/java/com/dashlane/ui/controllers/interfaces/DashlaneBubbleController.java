package com.dashlane.ui.controllers.interfaces;

import android.content.Context;

import wei.mark.standout.ui.Window;



public interface DashlaneBubbleController {

    boolean onBubbleClicked(Context context, int id, Window window);

    void onBubbleMoved(Context context, int id, Window window);

    void onBubbleClosed(Context context);

}
