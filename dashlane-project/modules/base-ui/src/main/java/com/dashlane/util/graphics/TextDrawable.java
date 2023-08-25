package com.dashlane.util.graphics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class TextDrawable extends Drawable implements BackgroundColorDrawable {

    private static final float TEXT_SIZE_FACTOR = 0.5f;

    private final String mText;
    private final Paint mBackgroundPaint;
    private final TextPaint mTextPaint;

    public TextDrawable(String text,
                        @ColorInt int textColor,
                        @ColorInt int backgroundColor,
                        @Nullable Typeface typeface) {
        mText = text;

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(backgroundColor);

        mTextPaint = new TextPaint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(textColor);
        if (typeface != null) {
            mTextPaint.setTypeface(typeface);
        }
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        mBackgroundPaint.setColor(color);
    }

    @Override
    @ColorInt
    public int getBackgroundColor() {
        return mBackgroundPaint.getColor();
    }

    @Nullable
    protected static String getFirstLetter(String label) {
        if (label == null || label.length() == 0) {
            return null;
        } else {
            return label.substring(0, 1).toUpperCase(Locale.US);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = getBounds();

        int size = Math.min(rect.height(), rect.width());

        drawBackground(canvas, rect, mBackgroundPaint);

        if (mText != null && mText.length() > 0) {
            adjustTextSize(size);

            float xPos = rect.exactCenterX();
            float yPos = (rect.exactCenterY() - ((mTextPaint.descent() + mTextPaint.ascent()) / 2f));
            canvas.drawText(mText, xPos, yPos, mTextPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mBackgroundPaint.setAlpha(alpha);
        mTextPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mBackgroundPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    protected void drawBackground(Canvas canvas, Rect bounds, Paint paint) {
        
    }

    protected float getTextSizeFactor() {
        return TEXT_SIZE_FACTOR;
    }

    private void adjustTextSize(int size) {
        float textSizeFactor = getTextSizeFactor();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; mText != null && i < mText.length(); i++) {
            sb.append("#");
        }
        float textWidth = mTextPaint.measureText(sb.toString());
        float currentTextSize = mTextPaint.getTextSize();
        float sizeWidthFactor = (size * textSizeFactor) / textWidth;
        float sizeHeightFactor = (size * textSizeFactor) / currentTextSize;
        float sizeFactor = Math.min(sizeHeightFactor, sizeWidthFactor);
        float newTextSize = sizeFactor * currentTextSize;
        if (Math.abs(newTextSize - currentTextSize) > 0.5) {
            
            mTextPaint.setTextSize(newTextSize);
        }
    }
}
