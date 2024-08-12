package com.dashlane.crashreport

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.fragment.app.Fragment
import com.dashlane.BuildConfig
import com.dashlane.crashreport.reporter.SentryCrashReporter
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.featureflipping.UserFeaturesChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.PrintWriter
import java.io.StringWriter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporterManager @Inject constructor(
    private val crashReporterLogger: CrashReporterLogger,
    private val globalPreferencesManager: GlobalPreferencesManager,
    @ApplicationContext private val mContext: Context,
    private val userFeaturesChecker: UserFeaturesChecker
) : CrashReporter {

    private val crashReporters: MutableList<Client> = ArrayList()
    private val crashTrace = CrashTrace(this)
    override val crashReporterId: String
        get() {
            var deviceId = globalPreferencesManager.getString(PREF_CRASH_DEVICE_ID)
            if (deviceId == null) {
                deviceId = UUID.randomUUID().toString()
                globalPreferencesManager.putString(PREF_CRASH_DEVICE_ID, deviceId)
            }
            return deviceId
        }

    override fun init(application: Application) {
        if (BuildConfig.DEBUG) {
            return 
        }

        crashTrace.autoTrackActivities(application)
        crashReporters.clear()

        
        addSentry()

        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
            
            crashReporterLogger.onCrashHappened(thread.toString(), throwable)
            if (!BuildConfig.CRASHLYTICS_ENABLED) {
                emailCrashReport(application, crashReporterId, throwable)
            }
            
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun addSentry() {
        if (crashReporters.any { it is SentryCrashReporter }) {
            return 
        }
        crashReporters.add(SentryCrashReporter(mContext, crashReporterId, userFeaturesChecker))
    }

    private fun emailCrashReport(application: Application, deviceId: String, throwable: Throwable) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setData(Uri.parse("mailto:"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Dashlane Crashed")

        val writer = StringWriter().apply {
            append("Manufacturer: ").append(Build.MANUFACTURER).append("\n")
            append("Model: ").append(Build.MODEL).append("\n")
            append("Crash Reporter Id: ").append(deviceId).append("\n")
            append("App Version Name: ").append(BuildConfig.VERSION_NAME).append("\n")
            append("App Version Code: ").append(BuildConfig.VERSION_CODE.toString()).append("\n")
            append("OS Version: ").append(Build.VERSION.SDK_INT.toString()).append("\n\n")
            append("StackTrace: ").append("\n")
        }

        val printWriter = PrintWriter(writer)
        throwable.printStackTrace(printWriter)
        writer.append("\n\nCause:")
        var cause = throwable.cause
        while (cause != null) {
            writer.append("\n")
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        intent.putExtra(Intent.EXTRA_TEXT, writer.toString())

        if (intent.resolveActivity(application.packageManager) != null) {
            val chooserIntent = Intent.createChooser(intent, "Send Dashlane Crash")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(chooserIntent)
        }
    }

    override fun logNonFatal(throwable: Throwable) {
        crashReporters.forEach { crashReporter ->
            crashReporter.logException(throwable)
        }
    }

    override fun addInformation(rawTrace: String) {
        crashTrace.add(rawTrace)
    }

    override fun addInformation(traceWithDate: String, rawTrace: String) {
        crashReporters.forEach { crashReporter ->
            crashReporter.log(traceWithDate, rawTrace)
        }
    }

    override fun addLifecycleInformation(fragment: Fragment?, cycle: String) {
        crashTrace.add(fragment, cycle)
    }

    interface Client {
        fun log(traceWithDate: String, rawTrace: String)

        fun logException(throwable: Throwable)
    }

    companion object {
        private const val PREF_CRASH_DEVICE_ID = "pref_crash_device_id"
    }
}
