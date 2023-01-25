package com.dashlane.autofill.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityEvent



class EventValidator(val context: Context) {

    private val currentActivityDetector = CurrentActivityDetector(context)

    fun isValid(event: AccessibilityEvent): Boolean {
        event.packageName ?: return false
        if (!isEventTypeAccepted(event)) {
            return false
        }
        return currentActivityDetector.isForCurrentActivity(event)
    }

    private fun isEventTypeAccepted(event: AccessibilityEvent): Boolean {
        return when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_ANNOUNCEMENT,
            AccessibilityEvent.TYPE_ASSIST_READING_CONTEXT,
            AccessibilityEvent.TYPE_GESTURE_DETECTION_START,
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED,
            AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START,
            AccessibilityEvent.TYPE_TOUCH_INTERACTION_START,
            AccessibilityEvent.TYPE_TOUCH_INTERACTION_END,
            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED,
            AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER,
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> true
            else -> false
        }
    }
}