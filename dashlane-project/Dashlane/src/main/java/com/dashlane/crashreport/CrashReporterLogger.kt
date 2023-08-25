package com.dashlane.crashreport

import javax.inject.Inject

class CrashReporterLogger @Inject constructor() {

    fun onCrashHappened(errorThread: String, throwable: Throwable) {
    }
}
