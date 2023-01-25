package com.dashlane.inapplogin

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.dashlane.BuildConfig



class InAppLoginByAccessibilityManager(private val context: Context) {

    fun isEnable() = isAccessibilityOn() && isDrawOnTopPermissionEnabled() && isOlderThanAndroidQ()

    fun isNoOpEnable() = isAccessibilityOn() && !isOlderThanAndroidQ()

    private fun isOlderThanAndroidQ() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    fun isDrawOnTopPermissionEnabled(): Boolean {
        return try {
            Settings.canDrawOverlays(context)
        } catch (e: Exception) {
            false
        }
    }

    fun startActivityToChooseProvider(context: Context?): Boolean {
        context ?: return false
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        return try {
            context.startActivity(intent)
            true
        } catch (ex: ActivityNotFoundException) {
            false
        }
    }

    private fun isAccessibilityOn(): Boolean {
        return try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val list = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            list?.any {
                it.resolveInfo.serviceInfo.packageName == BuildConfig.APPLICATION_ID &&
                        it.resolveInfo.serviceInfo.isEnabled
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}