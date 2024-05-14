package com.dashlane.ui.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.ColorInt;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;

import com.dashlane.ui.R;
import com.dashlane.util.ColorUtilsKt;
import com.dashlane.util.ContextUtilsKt;

public final class BadgeDrawerArrowDrawable extends DrawerArrowDrawable {

    private final Context mContext;
    private Paint mBadgePaint;
    private Paint mBackgroundPaint;
    private boolean mEnabled = false;
    private int mRadius;

    public BadgeDrawerArrowDrawable(Context context) {
        super(context);

        mContext = context;

        mBadgePaint = new Paint();
        mBadgePaint.setAntiAlias(true);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);

        mRadius = dpToPx(context, 4);

        setBackgroundColor(ContextUtilsKt.getThemeAttrColor(context, R.attr.colorPrimary));
    }

    public void setBackgroundColor(@ColorInt int color) {
        mBackgroundPaint.setColor(color);
        int colorOn = ColorUtilsKt.getColorOn(mContext, color);
        mBadgePaint.setColor(colorOn);
        setColor(colorOn);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (!mEnabled) {
            return;
        }

        final Rect bounds = getBounds();
        
        final float y = (bounds.height() - 3 * getBarThickness() - 2 * getGapSize()) / 2 + 1;
        final float x = (float) bounds.width() / 2 + getBarLength() / 2;
        canvas.drawCircle(x, y, mRadius + getGapSize(), mBackgroundPaint);
        canvas.drawCircle(x, y, mRadius, mBadgePaint);
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;
            invalidateSelf();
        }
    }

    private int dpToPx(Context context, int dps) {
        return Math.round(context.getResources().getDisplayMetrics().density * dps);
    }
}
