package com.dashlane.util

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window



class KeyboardVisibilityDetector(
    private val activity: Activity,
    private val listener: Listener
) : ViewTreeObserver.OnGlobalLayoutListener {

    constructor(activity: Activity, keyboardOverview: ((Int) -> Unit)?, keyboardHidden: (() -> Unit)?) :
            this(activity,
                object : Listener {
                    override fun onKeyboardOverView(keyboardHeight: Int) {
                        keyboardOverview?.invoke(keyboardHeight)
                    }

                    override fun onKeyboardHidden() {
                        keyboardHidden?.invoke()
                    }
                }
            )

    var keyboardHeight = 0

    init {
        activity.window.findViewById<View>(Window.ID_ANDROID_CONTENT).viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        
        val displayHeight = DeviceUtils.getScreenSize(activity)[1]

        val visibleRect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(visibleRect)

        val height = visibleRect.height()
        if (height == 0) {
            return 
        }
        val heightDiff = displayHeight - visibleRect.top - height
        if (heightDiff == keyboardHeight) {
            return 
        }
        keyboardHeight = heightDiff

        if (keyboardHeight > 0) {
            listener.onKeyboardOverView(heightDiff)
        } else {
            listener.onKeyboardHidden()
        }
    }

    fun onDestroy() {
        activity.window.findViewById<View>(Window.ID_ANDROID_CONTENT).viewTreeObserver
            .removeOnGlobalLayoutListener(this)
    }

    interface Listener {
        fun onKeyboardOverView(keyboardHeight: Int)
        fun onKeyboardHidden()
    }
}