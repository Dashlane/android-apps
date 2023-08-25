package com.dashlane.ui.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class CenterWrapperDrawable extends Drawable {

    private final Drawable mDrawable;
    private final Rect mOriginalBounds = new Rect();

    public CenterWrapperDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        mOriginalBounds.set(mDrawable.getBounds());

        int width = bounds.width();
        int height = bounds.height();
        int drawableWidth = mOriginalBounds.width();
        int drawableHeight = mOriginalBounds.height();
        int extraMarginX = width - drawableWidth;
        int extraMarginY = height - drawableHeight;
        int halfMarginX = extraMarginX / 2;
        int halfMarginY = extraMarginY / 2;

        mDrawable.setBounds(halfMarginX, halfMarginY, halfMarginX + drawableWidth, halfMarginY + drawableHeight);
        mDrawable.draw(canvas);
        mDrawable.setBounds(mOriginalBounds);
    }

    @Override
    public void setAlpha(int alpha) {
        mDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mDrawable.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return mDrawable.getOpacity();
    }

    @Override
    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return mDrawable.getIntrinsicWidth();
    }
}
