package com.dashlane.item.header;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.dashlane.R;
import com.dashlane.util.MeasureUtilKt;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;



@SuppressWarnings("unused")
public class LogoBehavior extends CoordinatorLayout.Behavior<ImageView> {
    private float mCustomFinalHeight;
    private int mStartXPosition;
    private float mStartToolbarPosition;
    private int mStartYPosition;
    private int mFinalYPosition;
    private int mStartHeight;
    private int mFinalXPosition;
    private float mChangeBehaviorPoint;

    public LogoBehavior(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LogoBehavior);
            mCustomFinalHeight = a.getDimension(R.styleable.LogoBehavior_finalHeight, 0);
            a.recycle();
        }
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull ImageView child, View dependency) {
        return dependency.getId() == R.id.bottom_title;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull ImageView child, @NonNull
            View dependency) {
        maybeInitProperties(child, dependency, parent);
        float expandedPercentageFactor = dependency.getY() / mStartToolbarPosition;

        if (expandedPercentageFactor < mChangeBehaviorPoint) {
            float heightFactor = (mChangeBehaviorPoint - expandedPercentageFactor) / mChangeBehaviorPoint;

            float distanceXToSubtract = ((mStartXPosition - mFinalXPosition)
                                         * heightFactor) + (float) child.getHeight() / 2;
            float distanceYToSubtract = ((mStartYPosition - mFinalYPosition)
                                         * (1f - expandedPercentageFactor)) + (float) child.getHeight() / 2;

            child.setX(mStartXPosition - distanceXToSubtract);
            child.setY(mStartYPosition - distanceYToSubtract);

            float heightToSubtract = ((mStartHeight - mCustomFinalHeight) * heightFactor);

            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            lp.width = (int) (mStartHeight - heightToSubtract);
            lp.height = (int) (mStartHeight - heightToSubtract);
            child.setLayoutParams(lp);
        } else {
            float distanceYToSubtract = ((mStartYPosition - mFinalYPosition)
                                         * (1f - expandedPercentageFactor)) + (float) mStartHeight / 2;

            child.setX(mStartXPosition - (float) child.getWidth() / 2);
            child.setY(mStartYPosition - distanceYToSubtract);

            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            lp.width = mStartHeight;
            lp.height = mStartHeight;
            child.setLayoutParams(lp);
        }
        return true;
    }

    private void maybeInitProperties(ImageView child, View dependency, CoordinatorLayout parent) {
        View icon = parent.findViewById(R.id.toolbar_icon);
        int margin = (int) MeasureUtilKt.dpToPx(child.getContext(), 28f);
        if (mFinalYPosition == 0)
            mFinalYPosition = icon.getTop() + margin;

        if (mFinalXPosition == 0)
            mFinalXPosition = icon.getLeft() + icon.getWidth() + margin;

        if (mStartHeight == 0)
            mStartHeight = child.getHeight();

        if (mStartXPosition == 0)
            mStartXPosition = Math.round(child.getX() + (float) child.getWidth() / 2);

        if (mStartYPosition == 0)
            mStartYPosition = Math.round(dependency.getY());

        if (mStartToolbarPosition < 0.1f)
            mStartToolbarPosition = dependency.getY();

        if (mChangeBehaviorPoint < 0.1f) {
            mChangeBehaviorPoint =
                    (child.getHeight() - mCustomFinalHeight) / (2f * (mStartYPosition - mFinalYPosition));
        }
    }
}