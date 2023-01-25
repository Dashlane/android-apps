package com.dashlane.masterpassword

import android.animation.Animator
import android.view.View
import android.view.ViewGroup
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.dashlane.R
import com.dashlane.util.getThemeAttrColor
import java.text.NumberFormat



class ChangeMasterPasswordProgressViewProxy(val view: View) {

    private val progressLayout =
        view.findViewById<ViewGroup>(R.id.progress_process_percent_layout)
    private val percentView =
        view.findViewById<TextView>(R.id.progress_process_percent_value)
    private val successAnimationView =
        view.findViewById<LottieAnimationView>(R.id.progress_animation).apply {
            addLottieOnCompositionLoadedListener {
                val color = context.getThemeAttrColor(R.attr.colorSecondary)

                addValueCallback(KeyPath("load", "**"), LottieProperty.STROKE_COLOR) { color }
                addValueCallback(KeyPath("load 2", "**"), LottieProperty.STROKE_COLOR) { color }
                addValueCallback(KeyPath("load 3", "**"), LottieProperty.STROKE_COLOR) { color }
                addValueCallback(KeyPath("Layer 1 copy Outlines", "**"), LottieProperty.COLOR) { color }
            }
        }

    private val messageView =
        view.findViewById<TextSwitcher>(R.id.progress_process_percent_message)

    private val progressNumberFormat: NumberFormat =
        NumberFormat.getPercentInstance()

    fun showLoader() {
        successAnimationView.setAnimation(R.raw.lottie_loading_indeterminate)
        successAnimationView.repeatCount = LottieDrawable.INFINITE
        successAnimationView.playAnimation()
        progressLayout.visibility = View.VISIBLE
    }

    fun hideLoader() {
        progressLayout.visibility = View.GONE
    }

    fun setMessage(text: CharSequence) {
        messageView.setText(text)
    }

    fun setProgress(@FloatRange(from = 0.0, to = 1.0) value: Float) {
        setProgress((value * 100).toInt())
    }

    fun setProgress(@IntRange(from = 0, to = 100) value: Int) {
        percentView.visibility = View.VISIBLE
        percentView.text = formatProgressString(value)
    }

    private fun formatProgressString(value: Int): String =
        progressNumberFormat.format(value / 100.0f)

    fun showSuccess(message: String, animate: Boolean, block: () -> Unit) {
        progressLayout.visibility = View.VISIBLE
        successAnimationView.finishAnimationAndRun {
            percentView.visibility = View.GONE
            messageView.setText(message)
            successAnimationView.removeAllAnimatorListeners()
            successAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationStart(animation: Animator) = Unit

                override fun onAnimationEnd(animation: Animator) {
                    block()
                }
            })
            successAnimationView.repeatCount = 0
            successAnimationView.setAnimation(R.raw.lottie_loading_success)
            if (animate) {
                successAnimationView.playAnimation()
            } else {
                successAnimationView.progress = 1f
                block()
            }
        }
    }

    private fun LottieAnimationView.finishAnimationAndRun(action: () -> Unit) {
        if (isAnimating) {
            repeatCount = 0
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationStart(animation: Animator) = Unit

                override fun onAnimationEnd(animation: Animator) {
                    removeAnimatorListener(this)
                    action()
                }
            })
        } else {
            action()
        }
    }
}