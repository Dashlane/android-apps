package com.dashlane.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.ViewGroup
import android.widget.TextView
import androidx.transition.Transition
import androidx.transition.TransitionValues



class TextColorTransition : Transition() {

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureTextColorValue(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureTextColorValue(transitionValues)
    }

    private fun captureTextColorValue(transitionValues: TransitionValues) {
        (transitionValues.view as? TextView)?.let { textView ->
            transitionValues.values[PROPNAME_TEXT_COLOR_TRANSITION] = textView.currentTextColor
        }
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        val startTextColor = startValues?.values?.get(PROPNAME_TEXT_COLOR_TRANSITION) as Int?
        val endTextColor = endValues?.values?.get(PROPNAME_TEXT_COLOR_TRANSITION) as Int?
        if (startTextColor == null || endTextColor == null || startTextColor == endTextColor) {
            return null
        }
        val textView = startValues!!.view as TextView
        textView.setTextColor(startTextColor)
        return ObjectAnimator.ofArgb(textView, "textColor", startTextColor, endTextColor)
    }

    companion object {
        private const val PROPNAME_TEXT_COLOR_TRANSITION = "dashlane:textColorTransition:textColor"
    }
}