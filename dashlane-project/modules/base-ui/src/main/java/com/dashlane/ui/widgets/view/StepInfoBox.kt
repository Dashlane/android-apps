package com.dashlane.ui.widgets.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.dashlane.ui.R
import com.dashlane.util.dpToPx
import com.google.android.material.button.MaterialButton

class StepInfoBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.Widget_Dashlane_Infobox
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {

    private val iconView by lazy(LazyThreadSafetyMode.NONE) { findViewById<ImageView>(R.id.infobox_icon)!! }
    private val iconTextView by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.infobox_icon_text)!! }
    private val textView by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.infobox_text)!! }
    private val buttonsLayout by lazy(LazyThreadSafetyMode.NONE) { findViewById<LinearLayout>(R.id.infobox_buttons_layout)!! }

    var text: CharSequence?
        get() = textView.text
        set(value) {
            textView.text = value
            textView.isVisible = !value.isNullOrEmpty()
        }

    lateinit var primaryButton: Button
    lateinit var secondaryButton: Button

    init {
        inflate(context, R.layout.widget_step_infobox, this)

        
        backgroundTintList = context.getColorStateList(R.color.step_infobox_background_tint)

        inflateButtons()
    }

    private fun inflateButtons() {
        buttonsLayout.removeAllViews()
        val (primaryStyle, secondaryStyle) = R.attr.materialButtonStyle to R.attr.borderlessButtonStyle
        primaryButton = MaterialButton(context, null, primaryStyle).apply {
            id = View.generateViewId()
            visibility = GONE
        }
        secondaryButton = MaterialButton(context, null, secondaryStyle).apply {
            id = View.generateViewId()
            visibility = GONE
        }
        buttonsLayout.addView(secondaryButton, generateCtaLayoutParams())
        buttonsLayout.addView(primaryButton, generateCtaLayoutParams())
    }

    fun setIcon(@DrawableRes icon: Int?, iconText: String?) {
        when {
            icon != null -> {
                iconView.setImageResource(icon)
                iconView.visibility = View.VISIBLE
                iconTextView.visibility = View.GONE
            }
            iconText != null -> {
                iconTextView.text = iconText
                iconView.visibility = View.GONE
                iconTextView.visibility = View.VISIBLE
            }
            else -> {
                iconView.setImageResource(R.drawable.ic_widget_infobox)
                iconView.visibility = View.VISIBLE
                iconTextView.visibility = View.GONE
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if (enabled) {
            primaryButton.apply {
                isClickable = true
                alpha = 1F
            }
            secondaryButton.apply {
                isClickable = true
                alpha = 1F
            }
        } else {
            primaryButton.apply {
                isClickable = false
                alpha = 0.6F
            }
            secondaryButton.apply {
                isClickable = false
                alpha = 0.6F
            }
        }
    }

    private fun generateCtaLayoutParams() =
        generateDefaultLayoutParams().apply {
            topMargin = context.dpToPx(8f).toInt()
        }
}