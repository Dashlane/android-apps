package com.dashlane.util

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build

@Suppress("DEPRECATION")
fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        getPackageInfo(packageName, flags)
    }
}

@Suppress("DEPRECATION")
fun PackageManager.getApplicationInfoCompat(packageName: String, flags: Int): ApplicationInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(flags.toLong()))
    } else {
        getApplicationInfo(packageName, flags)
    }
}

@Suppress("DEPRECATION")
fun PackageManager.getInstalledApplicationCompat(flags: Int): MutableList<ApplicationInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getInstalledApplications(PackageManager.ApplicationInfoFlags.of(flags.toLong()))
    } else {
        getInstalledApplications(flags)
    }
}

@Suppress("DEPRECATION")
fun PackageManager.resolveActivityCompat(intent: Intent, flags: Int): ResolveInfo? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        resolveActivity(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
    } else {
        resolveActivity(intent, 0)
    }
}

@Suppress("DEPRECATION")
fun PackageManager.queryIntentActivitiesCompat(intent: Intent, flags: Int): MutableList<ResolveInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
    } else {
        queryIntentActivities(intent, flags)
    }
}

fun PackageManager.getAppName(packageName: String): String? {
    return tryOrNull { this.getApplicationInfoCompat(packageName, 0) }?.let {
        this.getApplicationLabel(it).toString()
    }
}

fun PackageManager.getAppIcon(packageName: String): Drawable? {
    return tryOrNull { this.getApplicationInfoCompat(packageName, 0) }?.let {
        this.getApplicationIcon(it)
    }
}