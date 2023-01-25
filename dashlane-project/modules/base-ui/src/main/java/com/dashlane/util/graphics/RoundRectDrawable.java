package com.dashlane.util.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.dashlane.ui.R;
import com.dashlane.util.ColorUtilsKt;

import androidx.annotation.ColorInt;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.ColorUtils;

public class RoundRectDrawable extends Drawable implements BackgroundColorDrawable {

    private static final float ROUND_CORNER_SIZE_RATIO = 6 / 120f; 
    private final RectF mTempRect = new RectF();
    private final Path mClipPath = new Path();
    private final Paint mBackgroundPaint;
    private final Paint mStrokePaint;
    private final int mWidth;
    private final int mHeight;
    private boolean mPreferImageBackgroundColor;
    private Drawable mImage;
    private boolean mWithBorder = true;

    public RoundRectDrawable(Context context, int backgroundColor) {
        Resources resources = context.getResources();

        mWidth = resources.getDimensionPixelSize(R.dimen.material_splash_bitmap_width);
        mHeight = resources.getDimensionPixelSize(R.dimen.material_splash_bitmap_height);
        int borderSize = resources.getDimensionPixelSize(R.dimen.default_rounded_bitmap_border_size);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(borderSize);

        setBackgroundColor(backgroundColor);
    }

    public static RoundRectDrawable newWithImage(Context context, int backgroundColor, int imageOverResId) {
        RoundRectDrawable roundRectDrawable = new RoundRectDrawable(context, backgroundColor);
        Drawable secureNoteDrawable = AppCompatResources.getDrawable(context, imageOverResId);
        roundRectDrawable.setImage(secureNoteDrawable, false);
        return roundRectDrawable;
    }

    public boolean isWithBorder() {
        return mWithBorder;
    }

    public void setWithBorder(boolean withBorder) {
        mWithBorder = withBorder;
        invalidateSelf();
    }

    @Override
    @ColorInt
    public int getBackgroundColor() {
        return mBackgroundPaint.getColor();
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        mBackgroundPaint.setColor(backgroundColor);

        int strokeColor;
        if (ColorUtilsKt.hasGoodEnoughContrast(Color.BLACK, backgroundColor)) {
            strokeColor = Color.BLACK;
        } else {
            strokeColor = Color.WHITE;
        }

        mStrokePaint.setColor(ColorUtils.setAlphaComponent(strokeColor, 0x1a 
));

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        float halfStrokeWidth = mStrokePaint.getStrokeWidth() / 2;
        mTempRect.left = bounds.left + halfStrokeWidth;
        mTempRect.top = bounds.top + halfStrokeWidth;
        mTempRect.right = bounds.right - halfStrokeWidth;
        mTempRect.bottom = bounds.bottom - halfStrokeWidth;

        int cornerRadius = Math.round(bounds.height() * ROUND_CORNER_SIZE_RATIO);

        
        drawBackground(canvas, mTempRect, bounds, mBackgroundPaint);

        
        mClipPath.reset();
        mClipPath.addRoundRect(mTempRect, cornerRadius, cornerRadius, Path.Direction.CW);
        int saveCount = canvas.save();
        try {
            canvas.clipPath(mClipPath);
        } catch (UnsupportedOperationException ex) {
            
        }
        drawIcon(canvas);
        canvas.restoreToCount(saveCount);

        
        if (mWithBorder) {
            canvas.drawRoundRect(mTempRect, cornerRadius, cornerRadius, mStrokePaint);
        }
    }

    public Drawable getImage() {
        return mImage;
    }

    public void setImage(Drawable drawable) {
        setImage(drawable, mPreferImageBackgroundColor);
    }

    public void setImage(Drawable drawable, boolean useImageDominantColor) {
        mPreferImageBackgroundColor = useImageDominantColor;
        mImage = drawable;
        if (mPreferImageBackgroundColor && drawable != null) {
            setBackgroundColor(BitmapUtils.getDominantColorFromBorder(drawable));
        }
        invalidateSelf();
    }

    public void setPreferImageBackgroundColor(boolean preferImageBackgroundColor) {
        mPreferImageBackgroundColor = preferImageBackgroundColor;
    }

    @Override
    public void setAlpha(int alpha) {
        mBackgroundPaint.setAlpha(alpha);
        mStrokePaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mBackgroundPaint.setColorFilter(colorFilter);
        mStrokePaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return mBackgroundPaint.getAlpha();
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    protected void drawBackground(Canvas canvas, RectF rectF, Rect bounds, Paint paint) {
        int cornerRadius = Math.round(bounds.height() * ROUND_CORNER_SIZE_RATIO);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
    }

    private void drawIcon(Canvas canvas) {
        if (mImage == null) {
            return;
        }
        Rect bgBounds = getBounds();
        int intrinsicHeight = mImage.getIntrinsicHeight();
        int intrinsicWidth = mImage.getIntrinsicWidth();
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            mImage.setBounds(bgBounds);
            mImage.draw(canvas);
            return;
        }
        float ratio = intrinsicWidth / (float) intrinsicHeight;
        float bgRatio = bgBounds.width() / (float) bgBounds.height();
        if (ratio > bgRatio) {
            
            float newHeight = bgBounds.width() / ratio;
            int top = Math.round(bgBounds.exactCenterY() - newHeight / 2f);
            int bottom = top + Math.round(newHeight);
            mImage.setBounds(bgBounds.left, top, bgBounds.right, bottom);
        } else {
            float newWidth = ratio * bgBounds.height();
            int left = Math.round(bgBounds.exactCenterX() - newWidth / 2f);
            int right = left + Math.round(newWidth);
            mImage.setBounds(left, bgBounds.top, right, bgBounds.bottom);
        }
        mImage.draw(canvas);
    }

}
