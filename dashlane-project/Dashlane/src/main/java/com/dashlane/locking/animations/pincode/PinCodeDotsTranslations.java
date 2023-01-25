package com.dashlane.locking.animations.pincode;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

public class PinCodeDotsTranslations {

    private static final Interpolator INTERPOLATOR = new AccelerateInterpolator();

    private final View[] mDots; 
    private final float mBottomScreen;

    private final ViewPropertyAnimator[] mDotsAnimator;

    private final int mAnimationTotalDuration;

    public PinCodeDotsTranslations(View[] dots, View root, boolean disableAnimationEffect) {
        super();
        mDots = dots;
        mBottomScreen = root.getBottom();
        mDotsAnimator = new ViewPropertyAnimator[mDots.length];
        for (int i = 0; i < mDots.length; i++) {
            mDotsAnimator[i] = ((ViewGroup) mDots[i].getParent()).animate();
        }
        if (disableAnimationEffect) {
            mAnimationTotalDuration = 0;
        } else {
            mAnimationTotalDuration = 400;
        }
    }

    public int startAnimation() {
        long delayBetweenDots = (mAnimationTotalDuration / 2L) / mDots.length;
        long currentDelay = 0;
        for (int i = 0; i < mDots.length; i++) {
            mDotsAnimator[i].translationY(mBottomScreen)
                            .alpha(0)
                            .setDuration(mAnimationTotalDuration)
                            .setInterpolator(INTERPOLATOR)
                            .setStartDelay(currentDelay)
                            .start();
            currentDelay += delayBetweenDots;
        }
        if (mAnimationTotalDuration > 0) {
            return mAnimationTotalDuration + mAnimationTotalDuration / 2;
        } else {
            return 0;
        }
    }
}
