package com.dashlane.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dashlane.ui.activities.DashlaneActivity



abstract class AbstractActivityLifecycleListener : ActivityLifecycleListener {

    override fun onActivityUserInteraction(activity: DashlaneActivity) {
        
    }

    override fun onActivityResult(activity: DashlaneActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        
    }

    override fun onFirstActivityCreated() {
        
    }

    override fun onFirstActivityStarted() {
        
    }

    override fun onFirstActivityResumed() {
        
    }

    override fun onLastActivityPaused() {
        
    }

    override fun onLastActivityStopped() {
        
    }

    override fun onActivityPaused(activity: Activity) {
        
    }

    override fun onActivityResumed(activity: Activity) {
        
    }

    override fun onActivityStarted(activity: Activity) {
        
    }

    override fun onActivityDestroyed(activity: Activity) {
        
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        
    }

    override fun onActivityStopped(activity: Activity) {
        
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        
    }

    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        
    }
}