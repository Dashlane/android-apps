package com.dashlane.callbacks;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.adjust.sdk.Adjust;

public class AdjustActivityCallback implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        Adjust.onResume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Adjust.onPause();
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
