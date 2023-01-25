package com.dashlane.animations;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.dashlane.util.Constants;
import com.dashlane.util.TextUtil;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;



public class DecipheringTextAnimation implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private final WeakReference<TextView> mTextViewRef;
    private final ValueAnimator mValueAnimator;
    private final int mStartLength;
    private final int mDeltaLength;
    private final String mEndValue;

    @Nullable
    private OnAnimationEndListener mOnAnimationEndListener;

    public DecipheringTextAnimation(TextView mTextView, int durationMS, int mStartLength, int mEndLength, String
            endValue) {
        super();
        this.mTextViewRef = new WeakReference<>(mTextView);
        this.mStartLength = mStartLength;
        mDeltaLength = mEndLength - mStartLength;

        mValueAnimator = ValueAnimator.ofObject(new FloatEvaluator(), 0, 1);
        mValueAnimator.setDuration(durationMS);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.addUpdateListener(this);
        mValueAnimator.addListener(this);
        mEndValue = endValue;
    }

    public void setOnAnimationEndListener(@Nullable OnAnimationEndListener onAnimationEndListener) {
        mOnAnimationEndListener = onAnimationEndListener;
    }

    public void startAnimation() {
        mValueAnimator.start();
    }

    public void stopAnimation() {
        if (mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float interpolatedValue = (Float) animation.getAnimatedValue();
        String s = TextUtil.generateRandomString((int) (mStartLength + (interpolatedValue * mDeltaLength)),
                Constants.INDEX.ALPHAINDEX_NO_SPECIAL_CHAR.toString());
        mTextViewRef.get().setText(s);
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (mTextViewRef.get() != null) {
            mTextViewRef.get().setText(mEndValue);
        }
        if (mOnAnimationEndListener != null) {
            mOnAnimationEndListener.onAnimationEnd();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
