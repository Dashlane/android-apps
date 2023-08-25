package com.dashlane.util

import android.view.MotionEvent
import android.view.Window
import com.dashlane.login.lock.LockManager

class ActivityListenerWindowCallback(private val lockManager: LockManager, originalCallback: Window.Callback?) :
    DelegateWindowCallback(originalCallback) {

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        lockManager.setLastActionTimestampToNow()
        return super.dispatchTouchEvent(event)
    }
}