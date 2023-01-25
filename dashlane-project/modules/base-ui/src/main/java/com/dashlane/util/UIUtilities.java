package com.dashlane.util;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;

import com.dashlane.ui.R;

public final class UIUtilities {

    private UIUtilities() {
        throw new IllegalStateException("No instance");
    }

    public static View getToolbarLogoImageview(Toolbar mToolbar) {
        View view = null;
        for (int i = 0; i < mToolbar.getChildCount(); i++) {
            view = mToolbar.getChildAt(i);
            if ((view instanceof ImageView) && !(view instanceof ImageButton)) {
                return view;
            }
        }
        return view;
    }

    public static void setSwitchColor(SwitchCompat v, int thumbColor) {
        int trackColor;

        if (Color.alpha(thumbColor) == 0xFF) {
            trackColor = ColorUtils.setAlphaComponent(thumbColor, 138);
        } else {
            trackColor = thumbColor;
        }

        int uncheckThumbColor = v.getContext().getColor(R.color.switch_thumb_normal);
        int uncheckTrackColor = v.getContext().getColor(R.color.switch_track_normal);

        
        DrawableCompat.setTintList(v.getThumbDrawable(), new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        thumbColor,
                        uncheckThumbColor
                }));

        
        DrawableCompat.setTintList(v.getTrackDrawable(), new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        trackColor,
                        uncheckTrackColor
                }));
    }
}
