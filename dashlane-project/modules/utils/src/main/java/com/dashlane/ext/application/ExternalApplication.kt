package com.dashlane.ext.application

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.dashlane.util.getApplicationInfoCompat
import com.dashlane.util.queryIntentActivitiesCompat
import com.dashlane.util.tryOrNull

data class ExternalApplication(
    val packageName: String,
    val drawable: Drawable,
    val title: String
) {

    companion object {

        fun of(context: Context, packageName: String): ExternalApplication? {
            val packageManager = context.packageManager
            return tryOrNull {
                packageManager.getApplicationInfoCompat(packageName, 0)
            }?.let {
                val icon = packageManager.getApplicationIcon(it)
                val label = packageManager.getApplicationLabel(it).toString()
                ExternalApplication(packageName, icon, label)
            }
        }

        fun of(context: Context, intent: Intent): ExternalApplication? {
            val packageManager = context.packageManager
            return resolveDefault(
                packageManager,
                intent
            )?.let {
                val packageName = it.packageName
                val icon = it.loadIcon(packageManager)
                val title = it.loadLabel(packageManager).toString()
                return ExternalApplication(packageName, icon, title)
            }
        }

        @Suppress("DEPRECATION")
        private fun resolveDefault(
            packageManager: PackageManager,
            intent: Intent
        ): ActivityInfo? {
            return packageManager.queryIntentActivitiesCompat(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .firstOrNull()?.activityInfo
        }
    }
}