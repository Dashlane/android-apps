package com.dashlane.security.identitydashboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.dashlane.R
import com.dashlane.util.getThemeAttrColor
import java.math.RoundingMode
import java.text.NumberFormat

class PasswordAnalysisScoreViewProxy(view: View) {
    private val indeterminateAnimationView =
        view.findViewById<LottieAnimationView>(R.id.password_analysis_score_animation_indeterminate)!!.apply {
            addLottieOnCompositionLoadedListener {
                addValueCallback(
                    KEY_PATH_INDETERMINATE_BACKGROUND,
                    LottieProperty.STROKE_COLOR
                ) { context.getThemeAttrColor(R.attr.colorOnSurface) }
                addValueCallback(
                    KEY_PATH_INDETERMINATE_BACKGROUND,
                    LottieProperty.TRANSFORM_OPACITY
                ) { OPACITY_BACKGROUND }

                addValueCallback(KEY_PATH_INDETERMINATE, LottieProperty.STROKE_COLOR) {
                    context.getThemeAttrColor(R.attr.colorSecondary)
                }
            }
        }

    private val progressAnimationView =
        view.findViewById<LottieAnimationView>(R.id.password_analysis_score_animation_progress)!!.apply {
            addLottieOnCompositionLoadedListener {
                addValueCallback(
                    KEY_PATH_PROGRESS_BACKGROUND,
                    LottieProperty.STROKE_COLOR
                ) { context.getThemeAttrColor(R.attr.colorOnSurface) }
                addValueCallback(
                    KEY_PATH_PROGRESS_BACKGROUND,
                    LottieProperty.TRANSFORM_OPACITY
                ) { OPACITY_BACKGROUND }
            }
        }

    private val percentView = view.findViewById<TextView>(R.id.password_analysis_score_percent)!!
    private val labelView = view.findViewById<TextView>(R.id.password_analysis_score_label)!!

    fun setLabel(label: String?) {
        labelView.run {
            text = label
            visibility = if (label.isNullOrBlank()) View.GONE else View.VISIBLE
        }
    }

    fun showIndeterminate() {
        progressAnimationView.visibility = View.INVISIBLE
        indeterminateAnimationView.visibility = View.VISIBLE
        percentView.text = null
    }

    fun showProgress(progress: Float) {
        indeterminateAnimationView.visibility = View.INVISIBLE

        val value = progress.coerceAtMost(1f)
            .toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toFloat()

        progressAnimationView.run {
            cancelAnimation()

            if (value < 0) {
                this.progress = 0f
                percentView.text = "?"
            } else {
                val animator = ValueAnimator.ofFloat(0f, value).apply {
                    interpolator = FastOutSlowInInterpolator()
                    duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                    addUpdateListener { this@run.progress = it.animatedValue as Float }
                }

                addAnimatorListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        removeAnimatorListener(this)
                        animator.cancel()
                    }
                })

                animator.start()

                percentView.text = NumberFormat.getIntegerInstance().format(value * 100)
            }

            contentDescription = percentView.text
            visibility = View.VISIBLE
        }
    }

    companion object {
        private val KEY_PATH_INDETERMINATE_BACKGROUND = KeyPath("Shape Layer 1", "**")
        private val KEY_PATH_INDETERMINATE = KeyPath("loader", "**")
        private val KEY_PATH_PROGRESS_BACKGROUND = KeyPath("load 2", "**")

        private const val OPACITY_BACKGROUND = 38
    }
}