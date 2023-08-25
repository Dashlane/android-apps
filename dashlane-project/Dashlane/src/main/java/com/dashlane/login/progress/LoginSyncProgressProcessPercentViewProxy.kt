package com.dashlane.login.progress

import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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

class LoginSyncProgressProcessPercentViewProxy(val view: View) {

    private val progressLayout =
        view.findViewById<ViewGroup>(R.id.progress_process_percent_layout)
    private val percentView =
        view.findViewById<TextView>(R.id.progress_process_percent_value)
    private val progressAnimation =
        view.findViewById<LottieAnimationView>(R.id.progress_animation).apply {
            addLottieOnCompositionLoadedListener {
                addValueCallback(
                    KeyPath("**", "Fill 1"),
                    LottieProperty.COLOR
                ) { context.getThemeAttrColor(R.attr.colorOnBackground) }
            }
        }

    private val messageView =
        view.findViewById<TextSwitcher>(R.id.progress_process_percent_message)

    private val notesView =
        view.findViewById<TextView>(R.id.progress_process_percent_notes)

    private val progressNumberFormat: NumberFormat =
        NumberFormat.getPercentInstance()

    init {
        
        val inAnim = AnimationUtils.loadAnimation(view.context, android.R.anim.fade_in)
        val outAnim = AnimationUtils.loadAnimation(view.context, android.R.anim.fade_out)

        inAnim.startOffset = outAnim.duration
        inAnim.duration = outAnim.duration

        messageView.inAnimation = inAnim
        messageView.outAnimation = outAnim
    }

    fun showLoader() {
        progressAnimation.repeatCount = LottieDrawable.INFINITE
        progressLayout.visibility = View.VISIBLE
    }

    fun hideLoader() {
        progressLayout.visibility = View.GONE
    }

    fun setMessage(text: CharSequence) {
        
        if ((messageView.currentView as TextView).text == text) return
        messageView.setText(text)
    }

    fun setNotes(text: CharSequence) {
        notesView.text = text
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

    fun showSuccess(message: String, block: () -> Unit) {
        progressLayout.visibility = View.VISIBLE
        percentView.visibility = View.INVISIBLE
        notesView.visibility = View.INVISIBLE
        messageView.setText(message)
        block()
    }
}