package com.dashlane.util

import android.view.animation.Animation



val Animation.onLifecycleEvent: AnimationListenerDsl
    get() = AnimationListener().also { setAnimationListener(it) }



fun Animation.onLifecycleEvent(block: AnimationListenerDsl.() -> Unit) = onLifecycleEvent.apply(block)

interface AnimationListenerDsl {
    

    fun start(onAnimationStart: () -> Unit)

    

    fun end(onAnimationEnd: () -> Unit)

    

    fun repeat(onAnimationRepeat: () -> Unit)
}

private class AnimationListener : Animation.AnimationListener, AnimationListenerDsl {

    private var onAnimationStart: (() -> Unit)? = null
    private var onAnimationEnd: (() -> Unit)? = null
    private var onAnimationRepeat: (() -> Unit)? = null

    override fun start(onAnimationStart: () -> Unit) {
        this.onAnimationStart = onAnimationStart
    }

    override fun end(onAnimationEnd: () -> Unit) {
        this.onAnimationEnd = onAnimationEnd
    }

    override fun repeat(onAnimationRepeat: () -> Unit) {
        this.onAnimationRepeat = onAnimationRepeat
    }

    override fun onAnimationStart(animation: Animation?) = onAnimationStart?.invoke() ?: Unit
    override fun onAnimationEnd(animation: Animation?) = onAnimationEnd?.invoke() ?: Unit
    override fun onAnimationRepeat(animation: Animation?) = onAnimationRepeat?.invoke() ?: Unit
}