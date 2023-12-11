package com.dashlane.teamspaces.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Spinner;

import com.dashlane.teamspaces.R;
import com.dashlane.util.ContextUtilsKt;
import com.dashlane.util.DeviceUtils;

public class SpinnerUtil {

    @SuppressWarnings("ClickableViewAccessibility")
    private static final View.OnTouchListener ON_TOUCH_LISTENER = (v, event) -> {
        DeviceUtils.hideKeyboard(v);
        return false;
    };

    private SpinnerUtil() {
        
    }

    public static void disableSpinner(Spinner spinner) {
        spinner.setBackground(getBackground(spinner.getContext()));
        spinner.setFocusable(false);
        spinner.setClickable(false);
        spinner.setEnabled(false);
        spinner.setOnTouchListener(null);
        spinner.setBackgroundResource(0);
    }

    public static void enableSpinner(final Spinner spinner) {
        spinner.setClickable(true);
        spinner.setFocusable(true);
        spinner.setEnabled(true);
        spinner.setOnTouchListener(ON_TOUCH_LISTENER);
        spinner.setBackground(getBackground(spinner.getContext()));
    }

    private static Drawable getBackground(Context context) {
        Drawable bg = ContextUtilsKt.getThemeAttrDrawable(context, R.attr.editTextBackground).mutate();
        bg.setTint(ContextUtilsKt.getThemeAttrColor(context, R.attr.colorSecondary));
        return bg;
    }
}
