package com.dashlane.util

import android.os.Build

object EmulatorDetector {
    @JvmStatic
    fun doEmulatorCheck() {
        if (Build.PRODUCT == "sdk" && Build.DEVICE == "generic") {
            Constants.GCM.GCM_OFF = true
        }
    }
}