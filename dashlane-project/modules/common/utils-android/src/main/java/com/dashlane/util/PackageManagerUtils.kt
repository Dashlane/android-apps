package com.dashlane.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

fun PackageManager.getPackageInfoCompat(packageName: String): PackageInfo =
    getPackageInfo(packageName, 0)

fun PackageManager.getApplicationInfoCompat(packageName: String): ApplicationInfo =
    getApplicationInfo(packageName, 0)

fun PackageManager.getAppName(packageName: String): String? {
    return tryOrNull { this.getApplicationInfoCompat(packageName) }?.let {
        this.getApplicationLabel(it).toString()
    }
}

fun PackageManager.getAppIcon(packageName: String): Drawable? {
    return tryOrNull { this.getApplicationInfoCompat(packageName) }?.let {
        this.getApplicationIcon(it)
    }
}