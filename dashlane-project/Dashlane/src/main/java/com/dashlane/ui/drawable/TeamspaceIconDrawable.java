package com.dashlane.ui.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.dashlane.R;
import com.dashlane.util.graphics.TextDrawable;

import java.util.Locale;

import androidx.core.content.res.ResourcesCompat;



public class TeamspaceIconDrawable extends TextDrawable {

    private static final float TEXT_SIZE_FACTOR = 0.5f;
    private static final float BORDER_SIZE_RATIO = 2 / 24f; 

    private final Path mPath = new Path();
    private final Paint mPaintBorder = new Paint();
    private final Rect mTempRect = new Rect();
    private int mSize = -1;

    private TeamspaceIconDrawable(Context context, String text, int color) {
        super(text,
              Color.WHITE,
              color,
              ResourcesCompat.getFont(context, R.font.gt_walsheim_pro_bold));
        mPaintBorder.setColor(Color.BLACK);
        mPaintBorder.setAlpha(Math.round(0.24f * 256));
        mPaintBorder.setAntiAlias(true);
        mPaintBorder.setStyle(Paint.Style.STROKE);
    }

    public static TeamspaceIconDrawable newInstance(Context context, String label, int color) {
        String icoLabel;
        if (label == null || label.length() == 0) {
            icoLabel = null;
        } else {
            icoLabel = label.substring(0, 1).toUpperCase(Locale.US);
        }
        return new TeamspaceIconDrawable(context, icoLabel, color);
    }

    public void setSize(int size) {
        mSize = size;
        invalidateSelf();
    }

    @Override
    protected void drawBackground(Canvas canvas, Rect bounds, Paint paint) {
        fillBackgroundPath(bounds, mPath);
        canvas.drawPath(mPath, paint);

        mTempRect.set(bounds);
        int borderSize = Math.round((float) bounds.width() * BORDER_SIZE_RATIO);

        float halfBorderSize = borderSize / 2f;
        mTempRect.bottom -= halfBorderSize;
        mTempRect.top += halfBorderSize;
        mTempRect.right -= halfBorderSize;
        mTempRect.left += halfBorderSize;
        mPaintBorder.setStrokeWidth(borderSize);

        fillBackgroundPath(mTempRect, mPath);
        canvas.drawPath(mPath, mPaintBorder);
    }

    @Override
    protected float getTextSizeFactor() {
        return TEXT_SIZE_FACTOR;
    }

    private void fillBackgroundPath(Rect bounds, Path path) {
        int width = bounds.width();
        int height = bounds.height();
        float thirdWidth = width / 3f;
        float thirdHeight = height / 3f;
        path.reset();
        path.moveTo(bounds.left + thirdWidth, bounds.top);
        path.lineTo(bounds.right, bounds.top);
        path.lineTo(bounds.right, bounds.bottom - thirdHeight);
        path.lineTo(bounds.right - thirdWidth, bounds.bottom);
        path.lineTo(bounds.left, bounds.bottom);
        path.lineTo(bounds.left, bounds.top + thirdHeight);
        path.close();
    }

    @Override
    public int getIntrinsicWidth() {
        return mSize;
    }

    @Override
    public int getIntrinsicHeight() {
        return mSize;
    }
}
