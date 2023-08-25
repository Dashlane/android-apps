package com.dashlane.animations;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.View;

import com.dashlane.util.StatusBarUtils;

import java.lang.ref.WeakReference;

public class ColorTransformAnimation implements ValueAnimator.AnimatorUpdateListener {

    private final WeakReference<View> mRootView;
    private final WeakReference<Activity> mActivityRef;
    private final ValueAnimator mValueAnimator;

    public ColorTransformAnimation(Activity activity, View rootView, int fromColor, int toColor,
                                   int duration) {
        super();
        mRootView = new WeakReference<>(rootView);
        mActivityRef = new WeakReference<>(activity);
        mValueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor).setDuration(duration);
    }

    public void startAnimation() {
        mValueAnimator.addUpdateListener(this);
        mValueAnimator.start();
    }

    public void stopAnimation() {
        mValueAnimator.removeAllUpdateListeners();
        mValueAnimator.cancel();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int interpolatedColor = (Integer) animation.getAnimatedValue();
        if (mRootView.get() != null) {
            mRootView.get().setBackgroundColor(interpolatedColor);
        }
        if (mActivityRef.get() != null) {
            StatusBarUtils.setStatusBarColor(mActivityRef.get(),
                                             StatusBarUtils.computeStatusBarColor(interpolatedColor));

        }
    }
}
