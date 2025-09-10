package com.dashlane.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.dashlane.crashreport.CrashReporter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BaseUiFragment : Fragment() {
    @Inject
    lateinit var crashReporter: CrashReporter

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
}