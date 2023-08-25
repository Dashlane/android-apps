package com.dashlane.ui.widgets.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.use
import androidx.core.view.isVisible
import com.dashlane.ui.R
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrResourceId
import com.dashlane.util.updateConstraints
import com.google.android.material.button.MaterialButton
import kotlin.math.roundToInt

class Infobox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.infoboxStyle,
    defStyleRes: Int = R.style.Widget_Dashlane_Infobox
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    private val iconView by lazy(LazyThreadSafetyMode.NONE) { findViewById<ImageView>(R.id.infobox_icon)!! }
    private val titleView by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.infobox_title)!! }
    private val textView by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.infobox_text)!! }
    private val buttonsLayout by lazy(LazyThreadSafetyMode.NONE) { findViewById<LinearLayout>(R.id.infobox_buttons_layout)!! }

    var title: CharSequence?
        get() = titleView.text
        set(value) {
            titleView.text = value
            updateTypeStyle()
        }

    var text: CharSequence?
        get() = textView.text
        set(value) {
            textView.text = value
            
            
            textView.isVisible = !value.isNullOrEmpty()
        }

    lateinit var primaryButton: Button
    lateinit var secondaryButton: Button

    private var warning: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateToneStyle()
            }
        }

    private var subtleTint: ColorStateList? = null
    private var subtleTintBackground: ColorStateList? = null
    private var warningTint: ColorStateList? = null
    private var warningTintBackground: ColorStateList? = null
    private var tint: ColorStateList? = null
    private var backgroundTint: ColorStateList? = null

    init {
        inflate(context, R.layout.widget_infobox, this)

        context.obtainStyledAttributes(attrs, R.styleable.Infobox).use { ta ->
            titleView.text = ta.getString(R.styleable.Infobox_title)
            text = ta.getString(R.styleable.Infobox_android_text)
            warning = ta.getBoolean(R.styleable.Infobox_warning, false) ||
                    defStyleAttr == R.attr.warningInfoboxStyle
            
            inflateButtons()
            primaryButton.apply {
                text = ta.getString(R.styleable.Infobox_primaryButtonText)
                isVisible = !text.isNullOrEmpty()
            }
            secondaryButton.apply {
                text = ta.getString(R.styleable.Infobox_secondaryButtonText)
                isVisible = !text.isNullOrEmpty()
            }
        }

        
        context.obtainStyledAttributes(attrs, R.styleable.Infobox, R.attr.infoboxStyle, defStyleRes).use { ta ->
            subtleTint = ta.getColorStateList(R.styleable.Infobox_tint)!!
            subtleTintBackground = ta.getColorStateList(R.styleable.Infobox_backgroundTint)!!
        }
        context.obtainStyledAttributes(attrs, R.styleable.Infobox, R.attr.warningInfoboxStyle, defStyleRes).use { ta ->
            warningTint = ta.getColorStateList(R.styleable.Infobox_tint)!!
            warningTintBackground = ta.getColorStateList(R.styleable.Infobox_backgroundTint)!!
        }
        tint = if (warning) warningTint else subtleTint
        backgroundTint = if (warning) warningTintBackground else subtleTintBackground

        updateToneStyle()
        updateTypeStyle()
    }

    private fun updateToneStyle() {
        tint = if (warning) warningTint else subtleTint
        tint?.let { tint ->
            titleView.setTextColor(tint)
            textView.setTextColor(tint)
            iconView.imageTintList = tint
        }
        backgroundTint = if (warning) warningTintBackground else subtleTintBackground
        backgroundTint?.let { backgroundTintList = it }
    }

    private fun updateTypeStyle() {
        when {
            title.isNullOrEmpty() -> {
                alignIconBaselineToText()
                titleView.isVisible = false
                textView.setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceCaption))
            }
            else -> {
                alignIconBaselineToTitle()
                titleView.isVisible = true
                textView.setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceBody2))
            }
        }
        
        tint?.let { textView.setTextColor(it) }
    }

    private fun inflateButtons() {
        buttonsLayout.removeAllViews()
        val (primaryStyle, secondaryStyle) = when {
            warning -> R.attr.warningButtonStyle to R.attr.warningBorderlessButtonStyle
            else -> R.attr.materialButtonStyle to R.attr.borderlessButtonStyle
        }
        primaryButton = MaterialButton(context, null, primaryStyle).apply {
            id = R.id.infobox_button_primary
            visibility = GONE
        }
        secondaryButton = MaterialButton(context, null, secondaryStyle).apply {
            id = R.id.infobox_button_secondary
            visibility = GONE
        }
        buttonsLayout.addView(secondaryButton, generateCtaLayoutParams())
        buttonsLayout.addView(primaryButton, generateCtaLayoutParams())
    }

    private fun generateCtaLayoutParams() =
        generateDefaultLayoutParams().apply {
            topMargin = context.dpToPx(8f).toInt()
        }

    private fun alignIconBaselineToText() =
        updateConstraints {
            clear(iconView.id, ConstraintSet.TOP)
            connect(iconView.id, ConstraintSet.BASELINE, textView.id, ConstraintSet.BASELINE, 0)
            iconView.baseline = getBaseline(textView.paint.textSize)
        }

    private fun alignIconBaselineToTitle() =
        updateConstraints {
            clear(iconView.id, ConstraintSet.TOP)
            connect(iconView.id, ConstraintSet.BASELINE, titleView.id, ConstraintSet.BASELINE, 0)
            iconView.baseline = getBaseline(titleView.paint.textSize)
        }

    private fun getBaseline(textSize: Float) = when {
        textSize < 30f -> context.dpToPx(12f) 
        textSize < 38f -> context.dpToPx(13f)
        textSize < 45f -> context.dpToPx(14f)
        textSize < 51f -> context.dpToPx(15f)
        else -> context.dpToPx(16f) 
    }.roundToInt()
}
