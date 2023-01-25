package com.dashlane.csvimport

import android.content.Context
import android.content.Intent
import com.dashlane.help.HelpCenterLink
import com.dashlane.util.getApplicationInfoCompat
import com.dashlane.util.getPackageInfoCompat

object OnboardingChromeImportUtils {
    private const val CHROME_PACKAGE_NAME = "com.android.chrome"

    
    private const val CHROME_EXPORT_MIN_VERSION_MAJOR = 66

    internal fun launchChrome(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, HelpCenterLink.ARTICLE_IMPORT_FROM_CHROME.uri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .setPackage(CHROME_PACKAGE_NAME)

        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            true
        } else {
            false
        }
    }

    @JvmStatic
    fun hasChromeExport(context: Context): Boolean {
        return try {
            val applicationInfo = context.packageManager.getApplicationInfoCompat(CHROME_PACKAGE_NAME, 0)
            if (!applicationInfo.enabled) return false 
            val packageInfo = context.packageManager.getPackageInfoCompat(CHROME_PACKAGE_NAME, 0)
            val major = packageInfo.versionName.split('.').first().toInt()
            major >= CHROME_EXPORT_MIN_VERSION_MAJOR
        } catch (_: Throwable) {
            false
        }
    }
}