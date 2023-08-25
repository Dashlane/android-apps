package com.dashlane.animations;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

public class DecipheringAnimation {

    private final DecipheringTextAnimation mButtonTextAnimation;
    private final ColorTransformAnimation mColorTransformAnimation;

    public DecipheringAnimation(Activity activity, View rootView, TextView button,
                                int duration, String startValue, String endValue, int fromColor,
                                int toColor) {
        mButtonTextAnimation = new DecipheringTextAnimation(button, duration, startValue.length(), endValue.length(),
                                                            endValue);
        mColorTransformAnimation = new ColorTransformAnimation(activity, rootView, fromColor, toColor, duration);
    }

    public void startAnimation() {
        mButtonTextAnimation.startAnimation();
        mColorTransformAnimation.startAnimation();
    }

    public void stopAnimation() {
        mButtonTextAnimation.stopAnimation();
        mColorTransformAnimation.stopAnimation();
    }

    public void setOnAnimationEndListener(OnAnimationEndListener onAnimationEndListener) {
        mButtonTextAnimation.setOnAnimationEndListener(onAnimationEndListener);
    }
}
