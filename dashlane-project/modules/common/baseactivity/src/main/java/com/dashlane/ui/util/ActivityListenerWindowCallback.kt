package com.dashlane.ui.util

import android.view.MotionEvent
import android.view.Window
import com.dashlane.lock.LockTimeManager

class ActivityListenerWindowCallback(private val lockManager: LockTimeManager, originalCallback: Window.Callback?) :
    DelegateWindowCallback(originalCallback) {

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        lockManager.setLastActionTimestampToNow()
        return super.dispatchTouchEvent(event)
    }
}