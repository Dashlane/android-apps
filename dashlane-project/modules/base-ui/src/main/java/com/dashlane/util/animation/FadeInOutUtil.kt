package com.dashlane.util.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

fun View?.fadeOut() {
    if (this?.visibility == View.GONE) return
    this?.animate()
        ?.alpha(0.0f)
        ?.setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    this@fadeOut.visibility = View.GONE
                }
            }
        )
}

fun View?.fadeIn() {
    if (this?.visibility == View.VISIBLE) return
    this?.visibility = View.VISIBLE
    this?.animate()
        ?.alpha(1.0f)
        ?.setListener(
            
            object : AnimatorListenerAdapter() {}
        )
}
