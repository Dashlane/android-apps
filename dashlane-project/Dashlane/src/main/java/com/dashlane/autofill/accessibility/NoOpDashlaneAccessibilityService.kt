package com.dashlane.autofill.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent



class NoOpDashlaneAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit
}