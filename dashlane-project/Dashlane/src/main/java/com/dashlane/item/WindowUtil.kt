package com.dashlane.item

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.Window



fun Window.isOutOfBounds(context: Context, motionEvent: MotionEvent): Boolean {
    val x = motionEvent.x.toInt()
    val y = motionEvent.y.toInt()
    val slop = ViewConfiguration.get(context).scaledWindowTouchSlop
    val decorView = decorView
    return (x < -slop || y < -slop || x > decorView.width + slop || y > decorView.height + slop)
}