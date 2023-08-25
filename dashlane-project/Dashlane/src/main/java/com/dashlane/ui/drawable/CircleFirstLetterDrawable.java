package com.dashlane.ui.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.dashlane.R;
import com.dashlane.util.TextUtil;
import com.dashlane.util.graphics.TextDrawable;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class CircleFirstLetterDrawable extends TextDrawable {

    public static final int[] USERS_PROFILE_COLORS_RES = new int[]{
            R.color.users_profile_color0,
            R.color.users_profile_color1,
            R.color.users_profile_color2,
            R.color.users_profile_color3,
            R.color.users_profile_color4,
            R.color.users_profile_color5,
            R.color.users_profile_color6,
            R.color.users_profile_color7
    };

    private static final float TEXT_SIZE_FACTOR = 0.6f;

    private CircleFirstLetterDrawable(Context context, String text, @ColorRes int color) {
        super(text,
              Color.WHITE,
              ContextCompat.getColor(context, color),
              ResourcesCompat.getFont(context, R.font.roboto_regular));
    }

    public static CircleFirstLetterDrawable newInstance(Context context, String label) {
        return new CircleFirstLetterDrawable(context, getFirstLetter(label), getColor(label));
    }

    @Override
    protected void drawBackground(Canvas canvas, Rect bounds, Paint paint) {
        int size = Math.min(bounds.height(), bounds.width());
        float radius = size / 2f;
        canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), radius, paint);
    }

    @Override
    protected float getTextSizeFactor() {
        return TEXT_SIZE_FACTOR;
    }

    public static int calculateColorIndex(String label, int length) {
        return Math.abs(TextUtil.sumStringByChar(label)) % length;
    }

    public static int getColor(String label) {
        if (label == null || label.length() == 0) {
            label = "?";
        }
        int[] colors = USERS_PROFILE_COLORS_RES;
        return colors[calculateColorIndex(label, colors.length)];
    }
}
