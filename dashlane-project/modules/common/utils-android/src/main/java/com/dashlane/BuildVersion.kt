package com.dashlane

import android.os.Build

object BuildVersion {
    fun getSdkInt() = Build.VERSION.SDK_INT
}