package com.dashlane.util;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class ViewUtils {

    private ViewUtils() {
        
    }

    public static void removeOnGlobalLayoutListener(ViewTreeObserver tree, ViewTreeObserver.OnGlobalLayoutListener l) {
        tree.removeOnGlobalLayoutListener(l);
    }

    public static void setTransitionName(View view, String transitionName) {
        if (view != null) {
            view.setTransitionName(transitionName);
        }
    }

    public static String getTransitionName(View view) {
        if (view != null) {
            return view.getTransitionName();
        } else {
            return null;
        }
    }

    public static void setTransitionGroup(View view, boolean isGroup) {
        if (view instanceof ViewGroup) {
            ((ViewGroup) view).setTransitionGroup(isGroup);
        } else {
            throw new IllegalArgumentException("View must be a ViewGroup");
        }
    }
    
    public static void setBackground(View view, Drawable background) {
        view.setBackground(background);
    }
}
