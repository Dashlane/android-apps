package com.dashlane.util

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

fun View.onInitializeAccessibilityNodeInfo(onInitializeAccessibilityNodeInfo: (AccessibilityNodeInfoCompat) -> Unit) {
    ViewCompat.setAccessibilityDelegate(
        this,
        object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            onInitializeAccessibilityNodeInfo.invoke(info)
        }
    }
    )
}

@Suppress("DEPRECATION")
fun Context.announceForAccessibility(announcement: String, delayed: Boolean) {
    val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
    if (manager != null && manager.isEnabled) {
        val accessibilityEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AccessibilityEvent()
        } else {
            AccessibilityEvent.obtain()
        }
        accessibilityEvent.eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
        accessibilityEvent.className = javaClass.name
        accessibilityEvent.packageName = packageName
        accessibilityEvent.text.add(announcement)
        if (delayed) {
            Handler(Looper.getMainLooper()).postDelayed({ manager.sendAccessibilityEvent(accessibilityEvent) }, 1000)
        } else {
            manager.sendAccessibilityEvent(accessibilityEvent)
        }
    }
}