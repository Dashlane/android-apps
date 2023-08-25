package com.dashlane.ui.utils;

import android.content.Context;
import android.os.Bundle;

import com.dashlane.R;
import com.dashlane.ui.AbstractDashlaneSubwindow;
import com.dashlane.util.DeviceUtils;

import wei.mark.standout.ui.Window;

public class DashlaneSubwindowPositionUtils {

    public static final int OPTIMAL_POSITION_TOP = 0;
    public static final int OPTIMAL_POSITION_BOTTOM = 1;

    public static final int OPTIMAL_POSITION_LEFT = 0;
    public static final int OPTIMAL_POSITION_RIGHT = 1;
    public static final int OPTIMAL_POSITION_CENTER = 2;

    public static final int OPTIMAL_POSITION_SIDE_LEFT = 3;
    public static final int OPTIMAL_POSITION_SIDE_RIGHT = 4;

    private DashlaneSubwindowPositionUtils() {
        
    }

    public static Bundle getPositionBundle(Context context, Window window, int[] subwindowDimensions) {
        int[] locationRootScreen = new int[2];
        window.getLocationOnScreen(locationRootScreen);

        int bubbleY = locationRootScreen[1] + +window.getHeight() -
                      context.getResources().getDimensionPixelSize(R.dimen.dashlane_small_bubble_shadow_height);
        int optimalY = getSubwindowOptimalPositionY(context,
                                                    bubbleY,
                                                    window.getHeight(),
                                                    subwindowDimensions[1]);
        int optimalX = getSubwindowOptimalPositionX(context,
                                                    locationRootScreen[0] + window.getWidth(),
                                                    subwindowDimensions[0],
                                                    optimalY);
        return getPositionBundleOptimal(context, optimalX, optimalY, window, locationRootScreen, subwindowDimensions);
    }


    private static Bundle getPositionBundleOptimal(Context context, int optimalX, int optimalY, Window window, int[]
            locationRootScreen, int[] inAppWindowSize) {
        int optimalYPos = getOptimalYPosition(context, optimalY, window, locationRootScreen[1], inAppWindowSize[1]);
        int optimalXPos = getOptimalXPosition(context, optimalX, window, locationRootScreen[0], inAppWindowSize[0]);
        Bundle positions = new Bundle();
        positions.putInt(AbstractDashlaneSubwindow.DATA_POSX, optimalXPos);
        positions.putInt(AbstractDashlaneSubwindow.DATA_POSY, optimalYPos);
        positions.putInt(AbstractDashlaneSubwindow.DATA_OPTIM_AREA_X, optimalX);
        positions.putInt(AbstractDashlaneSubwindow.DATA_OPTIM_AREA_Y, optimalY);
        positions.putInt(AbstractDashlaneSubwindow.DATA_BUBBLE_POSITION_X, locationRootScreen[0]);
        positions.putInt(AbstractDashlaneSubwindow.DATA_BUBBLE_POSITION_Y, locationRootScreen[1]);
        positions.putIntArray(AbstractDashlaneSubwindow.DATA_SUBWINDOW_DIMENSIONS, inAppWindowSize);
        return positions;
    }

    private static int getOptimalYPosition(Context context, int optimalY, Window bubble, int parentWindowScreenPosY,
                                           int subwindowHeight) {
        switch (optimalY) {
            case OPTIMAL_POSITION_TOP:
                return parentWindowScreenPosY - subwindowHeight -
                       context.getResources().getDimensionPixelSize(R.dimen.dashlane_small_bubble_shadow_height);
            case OPTIMAL_POSITION_BOTTOM:
                return parentWindowScreenPosY + bubble.getHeight()
                       - context.getResources().getDimensionPixelSize(R.dimen.dashlane_small_bubble_shadow_height)
                       - DeviceUtils.getStatusBarHeight(context);
            case OPTIMAL_POSITION_CENTER:
                return parentWindowScreenPosY - (subwindowHeight / 2);
        }
        return 0;
    }

    private static int getOptimalXPosition(Context context, int optimalX, Window bubble, int parentWindowScreenPosX,
                                           int subwindowWidth) {
        switch (optimalX) {
            case OPTIMAL_POSITION_LEFT:
                return parentWindowScreenPosX + bubble.getWidth()
                       - subwindowWidth;
            case OPTIMAL_POSITION_RIGHT:
                return parentWindowScreenPosX;
            case OPTIMAL_POSITION_SIDE_LEFT:
                return parentWindowScreenPosX - subwindowWidth -
                       context.getResources().getDimensionPixelSize(R.dimen.dashlane_small_bubble_shadow_height);
            case OPTIMAL_POSITION_SIDE_RIGHT:
                return parentWindowScreenPosX + bubble.getWidth();
            case OPTIMAL_POSITION_CENTER:
                return 0;
        }
        return 0;
    }

    public static int getSubwindowOptimalPositionY(Context context, int bubblePositionY, int bubbleHeight,
                                                   int subwindowHeight) {
        int[] screenSize = DeviceUtils.getScreenSize(context);
        int topDist = Math.abs(bubblePositionY);
        int bottomDist = Math.abs(screenSize[1] - bubblePositionY - bubbleHeight);
        if (subwindowHeight < bottomDist) {
            return OPTIMAL_POSITION_BOTTOM;
        } else {
            if (subwindowHeight < topDist) {
                return OPTIMAL_POSITION_TOP;
            } else {
                if (topDist > bottomDist) {
                    return OPTIMAL_POSITION_CENTER;
                } else {
                    return OPTIMAL_POSITION_BOTTOM;
                }
            }
        }
    }

    public static int getSubwindowOptimalPositionX(Context context, int bubblePositionX, int subwindowWidth,
                                                   int optimalY) {
        int[] screenSize = DeviceUtils.getScreenSize(context);
        int leftDist = Math.abs(bubblePositionX);
        int rightDist = Math.abs(screenSize[0] - bubblePositionX);
        boolean isCenteredY = optimalY == OPTIMAL_POSITION_CENTER;

        
        if (isCenteredY) {
            
            if (rightDist < leftDist) {
                return OPTIMAL_POSITION_SIDE_LEFT;
            } else {
                return OPTIMAL_POSITION_SIDE_RIGHT;
            }
        }

        if (subwindowWidth < leftDist) {
            return OPTIMAL_POSITION_LEFT;
        } else if (subwindowWidth < rightDist) {
            return OPTIMAL_POSITION_RIGHT;
        } else {
            return OPTIMAL_POSITION_CENTER;
        }
    }

}
