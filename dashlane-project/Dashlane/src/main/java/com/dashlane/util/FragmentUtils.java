package com.dashlane.util;

import android.app.Activity;
import android.content.Context;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;

import androidx.fragment.app.Fragment;

public class FragmentUtils {

    private FragmentUtils() {

    }

    public static Fragment setEnterTransition(Context context, Fragment f, int transitionRes) {
        f.setEnterTransition(TransitionInflater.from(context).inflateTransition(transitionRes));
        return f;
    }

    public static Fragment setExitTransition(Context context, Fragment f, int transitionRes) {
        f.setExitTransition(
                TransitionInflater.from(context).inflateTransition(transitionRes));
        return f;
    }

    public static Fragment setReturnTransition(Context context, Fragment f, int transitionRes) {
        f.setReturnTransition(TransitionInflater.from(context).inflateTransition(transitionRes));
        return f;
    }

    public static Fragment setSharedElementEnterTransition(Context context, Fragment f, int transitionRes) {
        f.setSharedElementEnterTransition(TransitionInflater.from(context).inflateTransition(transitionRes));
        return f;
    }

    public static void excludeTargetFromTransition(Object transition, View view, boolean exclude) {
        ((Transition) transition).excludeTarget(view.getTransitionName(), exclude);
    }

    public static boolean illegalLifecycleState(Activity activity) {
        return activity == null || activity.isChangingConfigurations() || activity.isFinishing();
    }
}
