package com.dashlane.util.graphics

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ViewAnimator
import com.dashlane.ui.R
import com.dashlane.util.onLifecycleEvent



class ViewAnimatorPager : ViewAnimator {

    private val lastChildView: View
        get() = getChildAt(childCount - 1)

    private val inReturnAnimation: Int
    private val outReturnAnimation: Int
    private val popReturn: Boolean

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.ViewAnimatorPager)
        inReturnAnimation = a.getResourceId(R.styleable.ViewAnimatorPager_in_return_animation, 0)
        outReturnAnimation = a.getResourceId(R.styleable.ViewAnimatorPager_out_return_animation, 0)
        popReturn = a.getBoolean(R.styleable.ViewAnimatorPager_pop_return, true)
        a.recycle()
    }

    override fun showNext() {
        if (displayedChild >= childCount) return
        super.showNext()
    }

    override fun showPrevious() {
        if (displayedChild == 0) return
        val inAnimation = this.inAnimation
        setInAnimation(context, inReturnAnimation)
        this.inAnimation.onLifecycleEvent.end { this.inAnimation = inAnimation }
        val outAnimation = this.outAnimation
        setOutAnimation(context, outReturnAnimation)
        this.outAnimation.onLifecycleEvent.end {
            this.outAnimation = outAnimation
            if (popReturn) removeView(lastChildView)
        }
        super.showPrevious()
    }
}