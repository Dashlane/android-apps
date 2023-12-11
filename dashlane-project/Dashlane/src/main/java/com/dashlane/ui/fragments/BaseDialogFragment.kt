package com.dashlane.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.dashlane.crashreport.CrashReporter
import com.dashlane.login.lock.LockManager
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.applyScreenshotAllowedFlag
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.ActivityListenerWindowCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseDialogFragment : DialogFragment() {

    @Inject
    lateinit var crashReporter: CrashReporter

    @Inject
    lateinit var screenshotPolicy: ScreenshotPolicy

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var lockManager: LockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.CREATED)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.VIEW_CREATED)
    }

    override fun onStart() {
        super.onStart()
        crashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.STARTED)
    }

    override fun onResume() {
        super.onResume()
        disableScreenshotIfNeeded()
        crashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.RESUMED)
    }

    override fun onPause() {
        super.onPause()
        crashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.PAUSED)
    }

    override fun onStop() {
        super.onStop()
        crashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.STOPPED)
    }

    override fun onDestroy() {
        super.onDestroy()
        crashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.DESTROYED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        crashReporter.addLifecycleInformation(this, CrashReporter.Lifecycle.VIEW_DESTROYED)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window
        window!!.applyScreenshotAllowedFlag(screenshotPolicy)
        window.callback = ActivityListenerWindowCallback(lockManager, window.callback)
        return dialog
    }

    private fun disableScreenshotIfNeeded() {
        val dialog = dialog
        dialog?.let {
            val window = it.window
            window?.applyScreenshotAllowedFlag(screenshotPolicy)
        }
    }
}