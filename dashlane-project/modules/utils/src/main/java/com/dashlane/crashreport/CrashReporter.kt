package com.dashlane.crashreport

import android.app.Application
import androidx.fragment.app.Fragment

interface CrashReporter {

    val crashReporterId: String

    fun init(application: Application)
    fun logNonFatal(throwable: Throwable)

    fun addInformation(rawTrace: String)
    fun addInformation(traceWithDate: String, rawTrace: String)

    fun addLifecycleInformation(fragment: Fragment?, cycle: String)

    object Lifecycle {
        const val CREATED = "Created"
        const val DESTROYED = "Destroy"
        const val PAUSED = "Paused"
        const val RESUMED = "Resumed"
        const val STARTED = "Started"
        const val STOPPED = "Stopped"
        const val VIEW_CREATED = "View Created"
        const val VIEW_DESTROYED = "View Destroyed"
    }
}

interface CrashReporterComponent {

    val crashReporter: CrashReporter

    interface Application {
        val component: CrashReporterComponent
    }
}