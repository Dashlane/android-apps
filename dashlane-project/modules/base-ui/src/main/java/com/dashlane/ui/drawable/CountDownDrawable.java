package com.dashlane.ui.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

import com.dashlane.ui.R;




public class CountDownDrawable extends Drawable implements Animatable, Runnable {

    private final RectF mRectTemp = new RectF();
    private final Paint mPaint;
    private final Paint mBackgroundPaint;

    private boolean mRunning;
    private float mProgress;
    private long mIntervalPeriod;
    private long mStartTime;

    public CountDownDrawable(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(context.getColor(R.color.container_expressive_neutral_quiet_idle));
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
    }

    public void setIntervalInfo(long intervalPeriodMilliseconds, long timeRemainingMilliseconds) {
        mIntervalPeriod = intervalPeriodMilliseconds;
        
        mStartTime = getCurrentTime() - intervalPeriodMilliseconds + timeRemainingMilliseconds;
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        int height = bounds.height();
        int width = bounds.width();
        int margin = Math.max(height, width) - Math.min(height, width);

        int top;
        int bottom;
        int left;
        int right;

        if (margin == 0) {
            top = bounds.top;
            bottom = bounds.bottom;
            left = bounds.left;
            right = bounds.right;
        } else if (height > width) {
            top = bounds.top + margin / 2;
            bottom = bounds.bottom - margin / 2;
            left = bounds.left;
            right = bounds.right;
        } else {
            top = bounds.top;
            bottom = bounds.bottom;
            left = bounds.left + margin / 2;
            right = bounds.right - margin / 2;
        }

        mRectTemp.set(left, top, right, bottom);
        float origin = -90;
        double startAngle = mProgress * 360;
        double endAngle = 360;
        if (mProgress < 0.01) {
            endAngle = mProgress * 100 * 360 ;
            endAngle = (1-Math.cos(endAngle / 180 * Math.PI / 2.0)) * 180;
        }

        canvas.drawCircle(mRectTemp.centerX(), mRectTemp.centerY(), mRectTemp.width()/2, mBackgroundPaint);
        canvas.drawArc(mRectTemp, (float)(origin+startAngle), (float)(endAngle-startAngle), true, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        mRunning = true;
        run();
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }
        mRunning = false;
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    @Override
    public void run() {
        if (!isRunning()) {
            return;
        }
        float progress = 0;
        if (mIntervalPeriod != 0) {
            progress = (float)((double)(getCurrentTime() - mStartTime) % mIntervalPeriod / (double)mIntervalPeriod);
        }

        mProgress = progress;

        invalidateSelf();
        scheduleSelf(this, 16);
    }
}