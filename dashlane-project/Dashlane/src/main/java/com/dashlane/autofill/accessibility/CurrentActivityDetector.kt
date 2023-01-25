package com.dashlane.autofill.accessibility

import android.content.ComponentName
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.dashlane.util.tryOrNull



class CurrentActivityDetector(private val context: Context) {

    private val cachedValidActivity = mutableMapOf<Pair<String, String>, Boolean>()

    private var currentActivityPackageName: CharSequence? = null

    fun isForCurrentActivity(event: AccessibilityEvent): Boolean {
        if (event.packageName == currentActivityPackageName) {
            return true
        }
        return if (isActivityEvent(event)) {
            currentActivityPackageName = event.packageName
            true
        } else {
            false
        }
    }

    private fun isActivityEvent(event: AccessibilityEvent): Boolean {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return false
        }
        val packageName = event.packageName ?: return false
        val className = event.className ?: return false
        return isActivity(packageName.toString(), className.toString())
    }

    private fun isActivity(packageName: String, className: String): Boolean {
        return cachedValidActivity.getOrPut(Pair(packageName, className)) {
            isActivityFromPackageManager(packageName, className)
        }
    }

    private fun isActivityFromPackageManager(packageName: String, className: String): Boolean {
        return tryOrNull {
            context.packageManager.getActivityInfo(ComponentName(packageName, className), 0)
        } != null
    }
}