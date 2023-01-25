package com.dashlane.crashreport

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale



class CrashTrace(private val crashReporter: CrashReporter) {

    private val dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSSZ", Locale.ENGLISH)
    private var activityTracer: ActivityTracer? = null

    fun autoTrackActivities(application: Application) {
        if (activityTracer != null) {
            return 
        }
        activityTracer = ActivityTracer(this)
        application.registerActivityLifecycleCallbacks(activityTracer)
    }

    fun add(fragment: Fragment?, lifecycle: String) {
        val activityName: String?
        val fragmentName: String?
        if (fragment == null) {
            activityName = null
            fragmentName = activityName
        } else {
            fragmentName = fragment.javaClass.name
            activityName = getActivityName(fragment.activity)
        }
        add("[F] %s->%s (%s)".format(Locale.US, activityName, fragmentName, lifecycle))
    }

    fun add(trace: String?) {
        if (trace == null) {
            return
        }
        val dateFormat = dateFormat.format(ZonedDateTime.now())
        val sb = StringBuilder()
        sb.append(dateFormat).append(": ").append(trace)
        val traceWithDate = sb.toString()
        crashReporter.addInformation(traceWithDate, trace)
    }

    private fun add(activity: Activity, lifecycle: String) {
        add("[A] %s (%s)".format(Locale.US, getActivityName(activity), lifecycle))
    }

    private fun getActivityName(activity: Activity?): String? {
        return activity?.javaClass?.name
    }

    private class ActivityTracer internal constructor(private val mCrashTrace: CrashTrace) :
        Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            mCrashTrace.add(activity, CrashReporter.Lifecycle.CREATED)
        }

        override fun onActivityStarted(activity: Activity) {
            mCrashTrace.add(activity, CrashReporter.Lifecycle.STARTED)
        }

        override fun onActivityResumed(activity: Activity) {
            mCrashTrace.add(activity, CrashReporter.Lifecycle.RESUMED)
        }

        override fun onActivityPaused(activity: Activity) {
            mCrashTrace.add(activity, CrashReporter.Lifecycle.PAUSED)
        }

        override fun onActivityStopped(activity: Activity) {
            mCrashTrace.add(activity, CrashReporter.Lifecycle.STOPPED)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            
        }

        override fun onActivityDestroyed(activity: Activity) {
            mCrashTrace.add(activity, CrashReporter.Lifecycle.DESTROYED)
        }
    }
}
