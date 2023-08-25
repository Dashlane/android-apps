package com.dashlane.ui.controllers.impl;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.dashlane.R;
import com.dashlane.ui.DashlaneBubble;
import com.dashlane.ui.controllers.interfaces.DashlaneBubblePositionController;
import com.dashlane.util.DeviceUtils;

import wei.mark.standout.ui.Window;

public class DashlaneBubbleAnimatePositionController implements DashlaneBubblePositionController {


    @Override
    public void animateWindowToPosition(Context context, final Window window, Bundle positionBundle) {
        if (positionBundle == null || window == null) {
            return;
        }
        Rect fieldPos = positionBundle.getParcelable(DashlaneBubble.DATA_FORM_FIELD_BOUND);
        if (fieldPos == null) {
            return;
        }
        final int[] startPos = new int[2];
        window.getLocationOnScreen(startPos);
        startPos[0] = fieldPos.right - context.getResources().getDimensionPixelSize(R.dimen
                                                                                            .dashlane_small_bubble_width) /
                                       2;

        final int[] destPos = new int[]{
                fieldPos.right - context.getResources().getDimensionPixelSize(R.dimen.dashlane_small_bubble_width) +
                2 * context.getResources().getDimensionPixelSize(R.dimen
                                                                         .dashlane_small_bubble_alpha_left_right),
                fieldPos.centerY() - DeviceUtils.getStatusBarHeight(context) - context.getResources()
                                                                                      .getDimensionPixelSize(
                                                                                              R.dimen.dashlane_small_bubble_height) /
                                                                               2 + context.getResources()
                                                                                          .getDimensionPixelSize(
                                                                                                  R.dimen.dashlane_small_bubble_shadow_height)
        };
        animatePosition(window, startPos[0], startPos[1], destPos[0], destPos[1]);
    }

    @Override
    public void animatePosition(final Window window, final int fromX, final int fromY, final int toX, int toY) {
        final int deltaX = toX - fromX;
        final int deltaY = toY - fromY;
        ValueAnimator positionAnimator = ValueAnimator.ofObject(new FloatEvaluator(), 0, 1);
        positionAnimator.setDuration(600);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(animator -> {
            float interpolatedColor = (Float) animator.getAnimatedValue();
            int x = (int) (fromX + interpolatedColor * deltaX);
            int y = (int) (fromY + interpolatedColor * deltaY);
            if (window != null && window.visibility == Window.VISIBILITY_VISIBLE) {
                window.edit().setPosition(x, y).commit();
            }
        });
        positionAnimator.start();
    }
}
