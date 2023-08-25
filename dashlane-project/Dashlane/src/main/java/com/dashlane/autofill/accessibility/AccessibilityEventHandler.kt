package com.dashlane.autofill.accessibility

import android.view.accessibility.AccessibilityEvent

interface AccessibilityEventHandler {
    fun onNewEvent(event: AccessibilityEvent)
}